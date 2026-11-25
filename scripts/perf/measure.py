#!/usr/bin/env python3
"""
Análisis de desempeño de HU01 a HU08 sin Android Studio: recorre la app con adb y mide arranque, fluidez
(frames), CPU y memoria de cada pantalla. Solo usa la librería estándar de Python.

Uso:
    # Compara dos versiones, alternándolas en cada ronda para que ambas sufran igual las variaciones del emulador
    python3 scripts/perf/measure.py --runs 5 --out resultados.json \
        --apk antes=ruta/develop.apk --apk despues=ruta/feature.apk
    # Mide la versión ya instalada
    python3 scripts/perf/measure.py --runs 5 --label develop

Requisitos:
- Un emulador o dispositivo con la app (debug) instalada. Con `adb root` (imágenes de emulador "Google APIs")
  se fuerza el recolector de basura antes de medir memoria; sin root (un teléfono) se mide sin forzarlo y el
  resultado lo indica en "gc_forced".
- BackVynils corriendo con los datos de ./scripts/seed-backvynils.sh y ./scripts/perf/seed-perf-data.sh.
- Animaciones del dispositivo desactivadas (el script las desactiva).

Cada corrida borra los datos de la app (incluida la caché de Glide), así que todas parten del mismo estado.
El resultado es la mediana de las corridas válidas. El emulador debe tener aceleración por GPU
(`emulator -avd <avd> -gpu host`): con renderizado por software los frames miden el emulador y no la app.
Las métricas `ui_*` miden solo el hilo principal de la app y no dependen de la GPU; las de frames
totales (`janky_pct`, `p50/p90/p99_ms`) sí, y el script avisa cuando la GPU del emulador se vuelve lenta.
"""
import argparse
import json
import os
import re
import statistics
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ElementTree

PACKAGE = "co.edu.uniandes.vinilos"
ACTIVITY = f"{PACKAGE}/.ui.MainActivity"
TIMEOUT_S = 20
DETAIL_ROUND_TRIPS = 5
COVER_URL = "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg"
HAS_ROOT = False


def adb(*args: str) -> str:
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=True).stdout


def shell(command: str) -> str:
    return adb("shell", command)


# ---------- Interacción con la interfaz ----------

def ui_nodes() -> list:
    shell("uiautomator dump /sdcard/perf_ui.xml >/dev/null 2>&1")
    xml = adb("exec-out", "cat", "/sdcard/perf_ui.xml")
    try:
        return list(ElementTree.fromstring(xml).iter("node"))
    except ElementTree.ParseError:
        return []


def find(view_id: str) -> list:
    """Nodos visibles con el id dado, en orden de pantalla."""
    full_id = f"{PACKAGE}:id/{view_id}"
    return [node for node in ui_nodes() if node.get("resource-id") == full_id]


def center(node) -> tuple:
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
    return (x1 + x2) // 2, (y1 + y2) // 2


def wait_for(view_id: str, timeout_s: int = TIMEOUT_S) -> list:
    deadline = time.time() + timeout_s
    while time.time() < deadline:
        nodes = find(view_id)
        if nodes:
            return nodes
        time.sleep(0.3)
    screenshot = os.path.join(tempfile.gettempdir(), f"perf_timeout_{view_id}.png")
    with open(screenshot, "wb") as file:
        file.write(subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True, check=True).stdout)
    raise TimeoutError(f"No apareció {view_id} en {timeout_s} s (captura: {screenshot})")


def tap(node) -> None:
    x, y = center(node)
    shell(f"input tap {x} {y}")


def back() -> None:
    shell("input keyevent KEYCODE_BACK")


def hide_keyboard() -> None:
    """Cierra el teclado en pantalla solo si está abierto: con el teclado cerrado, "atrás" saldría de la pantalla."""
    # WindowManager permite consultar la visibilidad sin volcar el diagnóstico completo del teclado.
    # Se reintenta solo la lectura del estado, antes de decidir si se pulsa "atrás".
    for attempt in range(3):
        try:
            state = shell("dumpsys window windows")
            break
        except subprocess.CalledProcessError:
            if attempt == 2:
                raise
            time.sleep(0.3)
    windows = re.split(r"\n\s*Window #\d+ ", state)
    if any("mIsImWindow=true" in window and "isVisible=true" in window for window in windows):
        back()
        time.sleep(0.3)


def scroll_until(view_id: str, width: int, height: int, swipes: int = 6) -> list:
    """Desliza hacia abajo hasta que aparezca [view_id]; uiautomator solo ve lo que está en pantalla."""
    for _ in range(swipes):
        nodes = find(view_id)
        if nodes:
            return nodes
        shell(f"input swipe {width // 2} {int(height * 0.7)} {width // 2} {int(height * 0.35)} 250")
        time.sleep(0.4)
    return wait_for(view_id, timeout_s=5)


def scroll_list(width: int, height: int, times: int = 4) -> None:
    """Desliza hacia abajo y de vuelta hacia arriba, como un usuario recorriendo la lista."""
    x = width // 2
    for start, end in [(0.75, 0.25)] * times + [(0.25, 0.75)] * times:
        shell(f"input swipe {x} {int(height * start)} {x} {int(height * end)} 250")
        time.sleep(0.3)


# ---------- Métricas ----------

def pid() -> str:
    return shell(f"pidof {PACKAGE}").strip()


def cpu_ticks() -> int:
    """Tiempo de CPU del proceso (usuario + sistema) en ticks de 10 ms."""
    fields = shell(f"cat /proc/{pid()}/stat").split(")")[-1].split()
    return int(fields[11]) + int(fields[12])


def reset_frames() -> None:
    shell(f"dumpsys gfxinfo {PACKAGE} reset")


def frames() -> dict:
    out = shell(f"dumpsys gfxinfo {PACKAGE}")

    def number(pattern: str) -> float:
        match = re.search(pattern, out)
        return float(match.group(1)) if match else 0.0

    total = number(r"Total frames rendered: (\d+)")
    janky = number(r"Janky frames: (\d+)")
    return {
        "frames": total,
        "janky_pct": round(100 * janky / total, 1) if total else 0.0,
        "p50_ms": number(r"50th percentile: (\d+)ms"),
        "p90_ms": number(r"90th percentile: (\d+)ms"),
        "p99_ms": number(r"99th percentile: (\d+)ms"),
        # Tiempo de GPU por frame: si se dispara, el problema es del emulador y la corrida se descarta.
        "gpu_p90_ms": number(r"90th gpu percentile: (\d+)ms"),
    }


def main_thread_frames() -> dict:
    """
    Tiempo que el hilo principal de la app dedica a cada frame (entrada, animación, medida, layout y
    registro del dibujo), desde HandleInputStart hasta SyncQueued de `gfxinfo framestats`. No incluye el
    trabajo de la GPU, así que depende del código de la app y no de la aceleración gráfica del emulador.
    Android guarda los últimos 120 frames.
    """
    out = shell(f"dumpsys gfxinfo {PACKAGE} framestats")
    sections = out.split("---PROFILEDATA---")
    if len(sections) < 2:
        return {"ui_p50_ms": 0.0, "ui_p90_ms": 0.0, "ui_p99_ms": 0.0, "ui_over16_pct": 0.0}
    rows = [line.split(",") for line in sections[1].strip().splitlines()]
    header, data = rows[0], rows[1:]
    column = {name: index for index, name in enumerate(header)}
    times = sorted(
        (int(row[column["SyncQueued"]]) - int(row[column["HandleInputStart"]])) / 1e6
        for row in data
        if row[column["Flags"]] == "0" and int(row[column["SyncQueued"]]) > int(row[column["HandleInputStart"]]) > 0
    )
    if not times:
        return {"ui_p50_ms": 0.0, "ui_p90_ms": 0.0, "ui_p99_ms": 0.0, "ui_over16_pct": 0.0}

    def percentile(fraction: float) -> float:
        return round(times[min(len(times) - 1, int(fraction * len(times)))], 1)

    return {
        "ui_p50_ms": percentile(0.5),
        "ui_p90_ms": percentile(0.9),
        "ui_p99_ms": percentile(0.99),
        "ui_over16_pct": round(100 * sum(1 for value in times if value > 16) / len(times), 1),
    }


def memory() -> dict:
    """Memoria en MB y objetos vivos de la interfaz. Con root, primero fuerza el GC (SIGUSR1 en ART)."""
    if HAS_ROOT:
        shell(f"kill -10 {pid()}")
    time.sleep(1.5)
    out = shell(f"dumpsys meminfo {PACKAGE}")

    def kb(label: str) -> float:
        match = re.search(rf"{label}:\s+(\d+)", out)
        return round(int(match.group(1)) / 1024, 1) if match else 0.0

    def count(label: str) -> float:
        match = re.search(rf"\b{label}:\s+(\d+)", out)
        return float(match.group(1)) if match else 0.0

    return {
        "pss_mb": kb("TOTAL PSS"),
        "java_mb": kb("Java Heap"),
        "native_mb": kb("Native Heap"),
        "views": count("Views"),
        "activities": count("Activities"),
        "gc_forced": 1.0 if HAS_ROOT else 0.0,
    }


def measure(width: int, height: int, action) -> dict:
    """Ejecuta [action] y devuelve frames y CPU usados durante ella."""
    reset_frames()
    before = cpu_ticks()
    started = time.time()
    action()
    elapsed = time.time() - started
    cpu_ms = (cpu_ticks() - before) * 10
    return {**frames(), **main_thread_frames(), "cpu_ms": cpu_ms, "cpu_pct": round(100 * cpu_ms / 1000 / elapsed, 1)}


# ---------- Recorrido ----------

def detail_round_trips(list_item_id: str, detail_id: str, list_id: str) -> None:
    for index in range(DETAIL_ROUND_TRIPS):
        items = wait_for(list_item_id)
        tap(items[index % len(items)])
        wait_for(detail_id)
        back()
        try:
            wait_for(list_id, timeout_s=8)
        except TimeoutError:
            # A veces el "atrás" llega antes de que el detalle termine de abrir; se repite una vez.
            back()
            wait_for(list_id)


def form_round_trips() -> None:
    """HU07: abre el formulario, escribe la URL de la portada (dispara la vista previa) y cancela, sin crear nada."""
    for _ in range(DETAIL_ROUND_TRIPS):
        tap(wait_for("fab_new_album")[0])
        tap(wait_for("et_album_cover")[0])
        shell(f"input text '{COVER_URL}'")
        time.sleep(1.5)  # espera la vista previa (400 ms de pausa y la descarga)
        hide_keyboard()
        tap(wait_for("btn_create_album_cancel")[0])
        wait_for("fab_new_album")


def sheet_round_trips(width: int, height: int) -> None:
    """HU08: abre y cierra con "Cancelar" la hoja "Agregar track", sin agregar nada."""
    for _ in range(DETAIL_ROUND_TRIPS):
        tap(scroll_until("btn_add_track", width, height)[0])
        tap(wait_for("btn_add_track_cancel")[0])
        time.sleep(0.5)


def run_once(width: int, height: int) -> dict:
    shell(f"pm clear {PACKAGE}")
    startup = shell(f"am start -W -n {ACTIVITY}")
    startup_ms = float(re.search(r"TotalTime: (\d+)", startup).group(1))
    result = {"startup": {"total_ms": startup_ms}}

    # HU01 Listado de álbumes
    wait_for("card_album")
    time.sleep(2)  # deja que Glide termine de descargar las portadas visibles
    result["albums_scroll"] = measure(width, height, lambda: scroll_list(width, height))
    result["albums_memory"] = memory()

    # HU02 Detalle de álbum (ida y vuelta, para ver si las vistas destruidas se liberan)
    result["album_detail"] = measure(
        width, height, lambda: detail_round_trips("card_album", "tv_detail_name", "card_album"))
    result["album_detail_memory"] = memory()

    # HU03 Listado de artistas
    tap(wait_for("artistListFragment")[0])
    wait_for("card_artist")
    time.sleep(2)
    result["artists_scroll"] = measure(width, height, lambda: scroll_list(width, height))
    result["artists_memory"] = memory()

    # HU04 Detalle de artista
    result["artist_detail"] = measure(
        width, height, lambda: detail_round_trips("card_artist", "scroll_artist_detail", "card_artist"))
    result["artist_detail_memory"] = memory()

    # HU05 Listado de coleccionistas
    tap(wait_for("collectorListFragment")[0])
    wait_for("card_collector")
    result["collectors_scroll"] = measure(width, height, lambda: scroll_list(width, height))
    result["collectors_memory"] = memory()

    # HU06 Detalle de coleccionista
    result["collector_detail"] = measure(
        width, height, lambda: detail_round_trips("card_collector", "scroll_collector_detail", "card_collector"))
    result["collector_detail_memory"] = memory()

    # HU07 Formulario de creación de álbum
    tap(wait_for("albumListFragment")[0])
    wait_for("card_album")
    result["album_form"] = measure(width, height, form_round_trips)
    result["album_form_memory"] = memory()

    # HU08 Hoja "Agregar track" en el detalle de un álbum
    tap(wait_for("card_album")[0])
    wait_for("tv_detail_name")
    result["add_track_sheet"] = measure(width, height, lambda: sheet_round_trips(width, height))
    result["add_track_sheet_memory"] = memory()
    return result


def median(runs: list) -> dict:
    return {
        section: {metric: statistics.median(run[section][metric] for run in runs) for metric in runs[0][section]}
        for section in runs[0]
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--runs", type=int, default=5, help="corridas válidas por versión")
    parser.add_argument("--apk", action="append", default=[], metavar="ETIQUETA=RUTA",
                        help="versión a medir; con varias, se alternan en cada ronda (A, B, A, B...)")
    parser.add_argument("--label", default="instalada", help="etiqueta si no se pasa --apk (mide la app instalada)")
    parser.add_argument("--out", help="archivo JSON con las corridas y la mediana de cada versión")
    args = parser.parse_args()
    versions = [tuple(item.split("=", 1)) for item in args.apk] or [(args.label, None)]

    global HAS_ROOT
    subprocess.run(["adb", "root"], capture_output=True, text=True)
    time.sleep(2)
    adb("wait-for-device")
    HAS_ROOT = adb("shell", "id", "-u").strip() == "0"
    if not HAS_ROOT:
        print("aviso: sin adb root; la memoria se mide sin forzar el recolector de basura", flush=True)
    for setting in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
        shell(f"settings put global {setting} 0")
    width, height = map(int, re.findall(r"\d+", shell("wm size"))[-2:])

    valid = {label: [] for label, _ in versions}
    discarded = {label: 0 for label, _ in versions}
    round_number = 0
    while any(len(runs) < args.runs for runs in valid.values()) and round_number < args.runs * 3:
        round_number += 1
        for label, apk in versions:
            if len(valid[label]) >= args.runs:
                continue
            if apk:
                adb("install", "-r", apk)
            print(f"Ronda {round_number} · {label}...", flush=True)
            run = run_once(width, height)
            valid[label].append(run)
            if not is_valid(run):
                # Las métricas del hilo principal, CPU, memoria y arranque siguen siendo válidas;
                # las de frames totales (janky_pct, p50/p90/p99) quedan afectadas por la GPU del emulador.
                discarded[label] += 1
                print("  aviso: la GPU del emulador tardó más de 1 s por frame en esta corrida", flush=True)

    device = shell("getprop ro.product.model").strip() + " · API " + shell("getprop ro.build.version.sdk").strip()
    results = {}
    for label, runs in valid.items():
        summary = median(runs)
        results[label] = {"runs": runs, "runs_with_slow_gpu": discarded[label], "median": summary}
        print(f"\n{label} — mediana de {len(runs)} corridas, {discarded[label]} con la GPU del emulador lenta ({device})")
        for section, metrics in summary.items():
            print(f"  {section}: " + ", ".join(f"{name}={value:g}" for name, value in metrics.items()))

    if args.out:
        with open(args.out, "w", encoding="utf-8") as file:
            json.dump({"device": device, "versions": results}, file, ensure_ascii=False, indent=2)


def is_valid(run: dict) -> bool:
    """Una corrida es válida si la GPU del emulador respondió normal en todos los recorridos."""
    return all(metrics.get("gpu_p90_ms", 0) < 1000 for metrics in run.values())


if __name__ == "__main__":
    main()

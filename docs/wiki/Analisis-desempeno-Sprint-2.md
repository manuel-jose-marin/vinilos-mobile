# Análisis de desempeño · Sprint 2

Análisis de arranque, fluidez, CPU y memoria de **HU01 a HU05** ([#46](https://github.com/manuel-jose-marin/vinilos-mobile/issues/46)), y comparación antes y después de los cambios de [[Corrutinas y memoria|Corrutinas-y-memoria-Sprint-2]] (#114).

## Resumen

- **Arranque en frío:** 473 ms hasta la primera pantalla.
- **Scroll de los tres listados:** fluido. Entre el 3,5 % y el 4,3 % de frames lentos y el percentil 90 en 18-19 ms, frente a los 16,7 ms de un frame a 60 Hz.
- **Puntos a mejorar:**
  - El primer recorrido del catálogo de álbumes (HU01) tiene picos en el hilo principal: percentil 99 de 72 ms.
  - Al abrir el detalle de un artista (HU04), el 11 % de los frames supera 16 ms en el hilo principal.
- **Memoria:** estable. La app usa 71 MB en el catálogo y 82-88 MB después de recorrer las cinco pantallas. Siempre queda una sola actividad viva y las vistas no crecen después de 5 idas y vueltas a cada detalle, así que no hay fugas.
- **Cambios de #114:** no cambian de forma medible ninguna métrica con los datos actuales. El cambio que se probó para liberar imágenes aumentó la memoria y se revirtió.
- **Entorno de prueba:** el emulador del equipo tenía desactivada la aceleración gráfica, y con ella la app parecía 16 veces más lenta al hacer scroll. Ver [Hallazgo del entorno](#hallazgo-del-entorno).

## Entorno

| | |
|---|---|
| Equipo | MacBook Air (Apple M4, 16 GB RAM, macOS) |
| Dispositivo | Emulador Android 15 (API 35), arm64-v8a, 4 núcleos, 2 GB RAM, 1080 × 2400 a 420 dpi, iniciado con `-gpu host` |
| App | Compilación _debug_ de `develop` (98837d2) y de la rama de #114 |
| Backend | BackVynils local en Docker con `scripts/seed-backvynils.sh` y `scripts/perf/seed-perf-data.sh`: 12 álbumes, 22 artistas (17 músicos y 5 bandas) y 22 coleccionistas, para que cada listado ocupe más de una pantalla |

## Herramienta y método

Se usó el script [`scripts/perf/measure.py`](https://github.com/manuel-jose-marin/vinilos-mobile/blob/develop/scripts/perf/measure.py), que recorre la app con `adb` y lee las mismas fuentes de datos que Android Profiler:

| Métrica | Fuente | Qué indica |
|---|---|---|
| Arranque | `am start -W` (`TotalTime`) | Tiempo desde que se lanza la app hasta que dibuja su primera pantalla |
| Frames | `dumpsys gfxinfo` | Frames dibujados, porcentaje de frames lentos (_janky_) y percentiles del tiempo total por frame |
| Hilo principal por frame | `dumpsys gfxinfo framestats` (de `HandleInputStart` a `SyncQueued`) | Tiempo del hilo principal en cada frame: entrada, animación, medida, _layout_ y registro del dibujo. No incluye la GPU |
| CPU | `/proc/<pid>/stat` | Tiempo de CPU del proceso durante cada recorrido |
| Memoria | `dumpsys meminfo` después de forzar el recolector de basura | PSS total, _heap_ de Java, _heap_ nativo (donde Android guarda los bitmaps) y actividades y vistas vivas |

Cada corrida borra los datos de la app, incluida la caché de imágenes de Glide, y recorre:

1. **HU01:** abre la app (arranque en frío), espera el catálogo, desliza 4 veces hacia abajo y 4 hacia arriba, y mide la memoria.
2. **HU02:** abre y cierra 5 veces el detalle de un álbum, y mide la memoria.
3. **HU03:** abre Artistas, desliza igual que en HU01, y mide la memoria.
4. **HU04:** abre y cierra 5 veces el detalle de un artista, y mide la memoria.
5. **HU05:** abre Coleccionistas, desliza igual que en HU01, y mide la memoria.

Los resultados son la mediana de varias corridas. Para comparar dos versiones, el script las alterna en cada ronda (antes, después, antes, después...), así que las dos se miden en las mismas condiciones del emulador.

## Hallazgo del entorno

El emulador del equipo (`Vinilos_API_35`) tenía `hw.gpu.enabled = no`: dibujaba cada frame por software. Con esa configuración los frames medían el emulador y no la app.

| Medición de `develop` (5 corridas) | Sin aceleración gráfica | Con `-gpu host` |
|---|---:|---:|
| Arranque en frío | 4 249 ms | 473 ms |
| Scroll del catálogo: frames lentos | 97,7 % | 4,2 % |
| Scroll del catálogo: percentil 90 del frame | 300 ms | 18 ms |
| Scroll del catálogo: CPU del proceso | 63,8 % | 11,1 % |

Incluso con `-gpu host`, la GPU del emulador se volvió lenta después de unos minutos de uso continuo: el percentil 90 de GPU pasó de 18 ms a 4 950 ms por frame en `develop` y en #114 por igual. Por eso:

- Los tiempos totales de frame de la [línea base](#línea-base-de-hu01-a-hu05) vienen de la medición con la GPU estable.
- La [comparación antes y después](#antes-y-después-de-114) usa las métricas que no dependen de la GPU: hilo principal por frame, arranque, CPU y memoria. El script registra el tiempo de GPU de cada corrida y avisa cuando se dispara.

**Recomendación para el equipo:** iniciar el emulador con `emulator -avd <avd> -gpu host` (o activar la aceleración gráfica en la configuración del AVD) para las pruebas E2E y de desempeño.

## Línea base de HU01 a HU05

### Fluidez

Tiempo total por frame, de la medición de `develop` con la GPU estable (5 corridas):

| HU | Recorrido | Frames | Frames lentos | Percentil 50 | Percentil 90 | Percentil 99 | CPU |
|---|---|---:|---:|---:|---:|---:|---:|
| HU01 | Scroll del catálogo de álbumes | 164 | 4,2 % | 17 ms | 18 ms | 22 ms | 11,1 % |
| HU02 | 5 idas y vueltas al detalle de álbum | 47 | 51,1 % | 20 ms | 40 ms | 85 ms | 3,5 % |
| HU03 | Scroll del listado de artistas | 185 | 4,3 % | 17 ms | 18 ms | 21 ms | 12,8 % |
| HU04 | 5 idas y vueltas al detalle de artista | 55 | 56,4 % | 20 ms | 32 ms | 44 ms | 3,1 % |
| HU05 | Scroll del listado de coleccionistas | 198 | 3,5 % | 17 ms | 19 ms | 25 ms | 10,6 % |

Tiempo del hilo principal por frame, de la medición alternada (3 corridas de `develop`):

| HU | Recorrido | Percentil 50 | Percentil 90 | Percentil 99 | Frames sobre 16 ms |
|---|---|---:|---:|---:|---:|
| HU01 | Scroll del catálogo de álbumes | 3,3 ms | 15,5 ms | 71,7 ms | 8,8 % |
| HU02 | Idas y vueltas al detalle de álbum | 2,1 ms | 12,9 ms | 33,4 ms | 6,4 % |
| HU03 | Scroll del listado de artistas | 2,1 ms | 8,1 ms | 14,6 ms | 1,0 % |
| HU04 | Idas y vueltas al detalle de artista | 2,2 ms | 17,7 ms | 44,2 ms | 10,9 % |
| HU05 | Scroll del listado de coleccionistas | 0,7 ms | 5,1 ms | 9,5 ms | 0 % |

**Lectura:**

- **Los tres listados** se desplazan con fluidez: más del 95 % de los frames cumple los 16,7 ms.
- **HU05** es el más liviano porque no carga imágenes: el avatar son iniciales.
- **HU01** es el listado con más trabajo en el hilo principal (percentil 99 de 72 ms). La causa probable: cada fila de la cuadrícula tiene dos tarjetas (`MaterialCardView` con portada) que se inflan y se enlazan en el hilo principal, y en el primer recorrido el `RecyclerView` todavía está creando filas nuevas.
- **En los detalles (HU02 y HU04)** el porcentaje de frames lentos es alto porque cada ida y vuelta crea la pantalla completa: infla el _layout_, agrega las vistas de tracks, comentarios, premios o integrantes, y vuelve a crear el listado al regresar. El percentil 50 del hilo principal es de 2 ms: el costo está en los primeros frames de cada pantalla, no en toda la navegación. Además, el script lee la jerarquía de vistas (`uiautomator dump`) para saber cuándo cambió la pantalla, lo que agrega trabajo en estos recorridos, así que estos valores son una cota superior.

### Arranque y memoria

| Momento | PSS total | _Heap_ de Java | _Heap_ nativo | Vistas vivas | Actividades |
|---|---:|---:|---:|---:|---:|
| Catálogo de álbumes cargado (HU01) | 70,8 MB | 12,4 MB | 22,3 MB | 141 | 1 |
| Después de 5 idas y vueltas al detalle de álbum (HU02) | 82,2 MB | 13,7 MB | 24,6 MB | 105 | 1 |
| Listado de artistas (HU03) | 82,8 MB | 13,9 MB | 24,1 MB | 118 | 1 |
| Después de 5 idas y vueltas al detalle de artista (HU04) | 88,4 MB | 14,1 MB | 28,8 MB | 94 | 1 |
| Listado de coleccionistas (HU05) | 82,9 MB | 14,0 MB | 23,5 MB | 120 | 1 |

Arranque en frío: **473 ms** (mediana de 5 corridas).

**Lectura:**

- **No hay fugas de memoria.** Siempre hay una sola actividad viva. Después de 5 idas y vueltas a cada detalle, el número de vistas vivas no crece, sino que baja frente al listado inicial: las vistas de las pantallas cerradas se liberan. Esto confirma que liberar el binding y el adaptador en `onDestroyView` funciona.
- **El _heap_ de Java se mantiene entre 12 y 14 MB** en todo el recorrido.
- **El aumento de PSS al visitar los detalles (de 71 a 82-88 MB)** está sobre todo en memoria nativa y gráfica: las portadas y fotos que Glide guarda en su caché para no volver a descargarlas. Después de las pantallas con imágenes, se estabiliza alrededor de 83 MB.
- **En el detalle de artista, el _heap_ nativo varía entre 25 y 29 MB** de una corrida a otra, en las dos versiones de la app. Corresponde a imágenes en la caché de Glide, no a una fuga.

## Antes y después de #114

Medición alternada: 3 corridas de cada versión, en las mismas condiciones del emulador.

| Métrica | Recorrido | `develop` | #114 |
|---|---|---:|---:|
| Arranque en frío | | 2 496 ms | 2 465 ms |
| Hilo principal, percentil 90 | Scroll de álbumes (HU01) | 15,5 ms | 16,3 ms |
| Hilo principal, percentil 90 | Detalle de álbum (HU02) | 12,9 ms | 12,7 ms |
| Hilo principal, percentil 90 | Scroll de artistas (HU03) | 8,1 ms | 10,1 ms |
| Hilo principal, percentil 90 | Detalle de artista (HU04) | 17,7 ms | 17,3 ms |
| Hilo principal, percentil 90 | Scroll de coleccionistas (HU05) | 5,1 ms | 3,2 ms |
| CPU | Scroll de álbumes (HU01) | 1 320 ms | 1 230 ms |
| PSS total | Después de recorrer HU01 a HU05 | 86,1 MB | 86,2 MB |
| _Heap_ de Java | Después de recorrer HU01 a HU05 | 16,0 MB | 15,9 MB |

En esta medición el arranque es de unos 2,5 s en las dos versiones porque la GPU del emulador ya estaba lenta (ver [Hallazgo del entorno](#hallazgo-del-entorno)); lo que importa es la comparación entre columnas.

**Conclusión:** con los datos actuales (12 a 22 elementos por lista), los cambios de #114 no producen diferencias medibles. Las variaciones entre columnas son del mismo tamaño que las variaciones entre corridas de una misma versión. Era lo esperado:

- Convertir el JSON fuera del hilo principal protege a la app cuando las respuestas crezcan, por ejemplo con los álbumes y tracks que agregarán HU07 y HU08.
- Cancelar la carga anterior al reintentar corrige un comportamiento incorrecto, que verifican las pruebas unitarias, más que un costo de rendimiento.

### Cambio probado y revertido

La primera versión de #114 liberaba la imagen de cada fila reciclada con `Glide.with(view).clear(view)`. La medición alternada (5 corridas de cada versión) mostró:

| Momento | `develop` | Con `clear()` |
|---|---:|---:|
| _Heap_ nativo en el listado de artistas | 24,5 MB | 28,0 MB |
| _Heap_ nativo después del detalle de artista | 25,1 MB | 32,2 MB |
| PSS total al final del recorrido | 86,1 MB | 89,6 MB |

El aumento se repitió en las 5 corridas, sin mejorar ninguna otra métrica: `clear()` devuelve el bitmap al _pool_ de Glide, que lo conserva para reutilizarlo. El cambio se revirtió antes de integrar #114. Sin él, el _heap_ nativo del listado de artistas volvió al nivel de `develop` (24,7 MB frente a 25,1 MB).

## Recomendaciones para el Sprint 3

| Prioridad | Recomendación | Motivo |
|---|---|---|
| Alta | Iniciar el emulador con aceleración gráfica (`-gpu host`) para pruebas E2E y de desempeño | Sin ella la app parece 16 veces más lenta al hacer scroll y los resultados no son comparables |
| Media | Perfilar el scroll del catálogo con Android Studio (CPU Profiler) para confirmar la causa de los picos; probar precargar las portadas con `RecyclerViewPreloader` de Glide y simplificar la tarjeta de álbum | HU01 tiene los picos más altos del hilo principal (percentil 99 de 72 ms) |
| Media | En el detalle de artista, reutilizar el adaptador del carrusel en vez de crear uno nuevo cada vez que llega el contenido | HU04 tiene el mayor porcentaje de frames del hilo principal sobre 16 ms (11 %) |
| Baja | Pedir al backend el autor dentro de cada comentario | HU02 descarga todos los coleccionistas solo para mostrar el autor de los comentarios; el costo crece con el número de coleccionistas |
| Baja | Volver a correr este análisis cuando estén HU06 a HU08 | Crear álbumes y agregar tracks aumentará el tamaño de las respuestas |

## Cómo repetir la medición

```bash
# 1. Backend con los datos de prueba (una sola vez, sobre una base recién creada)
./scripts/seed-backvynils.sh
./scripts/perf/seed-perf-data.sh

# 2. Emulador con aceleración gráfica
emulator -avd <avd> -gpu host

# 3. Compilar las versiones a comparar y medir (alterna las versiones en cada ronda)
./gradlew assembleDebug
python3 scripts/perf/measure.py --runs 5 --out resultados.json \
  --apk antes=ruta/a/develop.apk --apk despues=app/build/outputs/apk/debug/app-debug.apk
```

Los datos de cada medición de este análisis están en [`docs/wiki/perf`](https://github.com/manuel-jose-marin/vinilos-mobile/tree/develop/docs/wiki/perf):

| Archivo | Medición |
|---|---|
| `sprint2-medicion-sin-gpu.txt` | `develop` con la configuración original del emulador, sin aceleración gráfica (5 corridas) |
| `sprint2-medicion0-develop-gpu.txt` | `develop` con la GPU estable: línea base de fluidez, arranque y memoria (5 corridas) |
| `sprint2-medicion1-con-glide-clear.json` | `develop` frente a la primera versión de #114, con `Glide.clear()` (5 corridas de cada una) |
| `sprint2-medicion2-final.json` | `develop` frente a la versión final de #114 (3 corridas de cada una) |

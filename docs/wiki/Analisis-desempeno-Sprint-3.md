# Análisis de desempeño · Sprint 3

Análisis de arranque, fluidez, CPU y memoria de las ocho historias del MVP, **HU01 a HU08** ([#67](https://github.com/manuel-jose-marin/vinilos-mobile/issues/67)), con la app completa. Sigue el método del [[análisis del Sprint 2|Analisis-desempeno-Sprint-2]] y agrega los recorridos de HU06, HU07 y HU08 que describe la [[estrategia de pruebas del Sprint 3|Estrategia-pruebas-Sprint-3]].

## Resumen

- **Memoria sin fugas.** Siempre queda una sola actividad viva. Después de cada serie de idas y vueltas, las vistas vivas bajan en vez de crecer: de 141 en el catálogo a 77 al final del recorrido. El _heap_ de Java se mantiene entre 13 y 19 MB.
- **Los tres listados (HU01, HU03 y HU05) son fluidos en el hilo principal:** ningún frame pasa de 16 ms y el percentil 99 está entre 6 y 13 ms.
- **La hoja "Agregar track" (HU08) es liviana:** percentil 90 de 11,6 ms en el hilo principal y 2,9 % de frames sobre 16 ms.
- **El formulario de álbum (HU07) es el punto de más memoria:** 102 MB de PSS, por las imágenes de la vista previa de la portada. La memoria baja al salir del formulario.
- **Punto a mejorar:** al abrir los detalles de álbum, artista y coleccionista (HU02, HU04 y HU06), entre el 20 % y el 24 % de los frames pasa de 16 ms en el hilo principal. Quedó registrado en [#183](https://github.com/manuel-jose-marin/vinilos-mobile/issues/183).
- **Entorno:** en las 5 corridas la GPU del emulador se volvió lenta, como pasó en el Sprint 2. Por eso los tiempos totales de frame de los detalles no se usan, y el análisis se apoya en las métricas que no dependen de la GPU: hilo principal, CPU, memoria y arranque.

## Entorno

| | |
|---|---|
| Equipo | MacBook Pro con procesador Intel, 12 núcleos (macOS) |
| Dispositivo | Emulador Android 15 (API 35), `sdk_gphone64_x86_64`, 2 núcleos, iniciado con `-gpu host` y las animaciones desactivadas |
| App | Compilación _debug_ de `develop` con las pruebas de HU08, las micro-optimizaciones del Sprint 3 y este script integrados (24 de noviembre de 2026) |
| Backend | BackVynils local en Docker, con base recién creada y los datos de `scripts/seed-backvynils.sh` y `scripts/perf/seed-perf-data.sh`: 12 álbumes, 22 artistas y 22 coleccionistas |
| Datos crudos | [`perf/sprint3-medicion-develop.json`](https://github.com/manuel-jose-marin/vinilos-mobile/blob/develop/docs/wiki/perf/sprint3-medicion-develop.json) (las 5 corridas y la mediana) y [`perf/sprint3-medicion-develop.txt`](https://github.com/manuel-jose-marin/vinilos-mobile/blob/develop/docs/wiki/perf/sprint3-medicion-develop.txt) (salida del script) |

## Herramienta y método

Se usó [`scripts/perf/measure.py`](https://github.com/manuel-jose-marin/vinilos-mobile/blob/develop/scripts/perf/measure.py), que recorre la app con `adb` y lee las mismas fuentes que Android Profiler:

| Métrica | Fuente | Qué indica |
|---|---|---|
| Arranque | `am start -W` (`TotalTime`) | Tiempo desde que se lanza la app hasta que dibuja su primera pantalla |
| Frames | `dumpsys gfxinfo` | Frames dibujados, porcentaje de frames lentos y percentiles del tiempo total por frame. Incluye la GPU |
| Hilo principal por frame | `dumpsys gfxinfo framestats` (de `HandleInputStart` a `SyncQueued`) | Tiempo del hilo principal en cada frame: entrada, animación, medida, _layout_ y registro del dibujo. No incluye la GPU |
| CPU | `/proc/<pid>/stat` | Tiempo de CPU del proceso durante cada recorrido |
| Memoria | `dumpsys meminfo` después de forzar el recolector de basura | PSS total, _heap_ de Java, _heap_ nativo (donde Android guarda los bitmaps) y actividades y vistas vivas |

Cada corrida borra los datos de la app, incluida la caché de imágenes, y recorre en orden:

1. **HU01:** abre la app (arranque en frío), espera el catálogo y desliza 4 veces hacia abajo y 4 hacia arriba.
2. **HU02:** abre y cierra 5 veces el detalle de un álbum.
3. **HU03:** abre Artistas y desliza igual que en HU01.
4. **HU04:** abre y cierra 5 veces el detalle de un artista.
5. **HU05:** abre Coleccionistas y desliza igual que en HU01.
6. **HU06:** abre y cierra 5 veces el detalle de un coleccionista.
7. **HU07:** 5 veces abre el formulario de álbum, escribe la URL de la portada (carga la vista previa) y cancela.
8. **HU08:** en el detalle de un álbum, abre y cancela 5 veces la hoja "Agregar track".

Después de cada paso mide la memoria. Los recorridos de HU07 y HU08 no guardan datos. Los resultados son la mediana de 5 corridas.

## Fluidez

### Hilo principal

Tiempo del hilo principal por frame, mediana de las 5 corridas. No depende de la GPU, así que vale para todos los recorridos.

| HU | Recorrido | Frames | Percentil 50 | Percentil 90 | Percentil 99 | Frames sobre 16 ms | CPU del proceso |
|---|---|---:|---:|---:|---:|---:|---:|
| HU01 | Scroll del catálogo de álbumes | 169 | 1,5 ms | 3,1 ms | 9,9 ms | 0 % | 28,9 % |
| HU02 | 5 idas y vueltas al detalle de álbum | 48 | 4,1 ms | 99,1 ms | 147,0 ms | 22,4 % | 8,5 % |
| HU03 | Scroll del listado de artistas | 193 | 1,5 ms | 5,9 ms | 12,6 ms | 0 % | 31,8 % |
| HU04 | 5 idas y vueltas al detalle de artista | 62 | 5,7 ms | 58,2 ms | 214,4 ms | 20,0 % | 7,0 % |
| HU05 | Scroll del listado de coleccionistas | 234 | 1,3 ms | 3,6 ms | 6,0 ms | 0 % | 25,8 % |
| HU06 | 5 idas y vueltas al detalle de coleccionista | 45 | 4,1 ms | 96,7 ms | 122,9 ms | 23,9 % | 4,7 % |
| HU07 | 5 veces formulario de álbum con vista previa | 498 | 1,6 ms | 4,7 ms | 107,3 ms | 2,5 % | 9,2 % |
| HU08 | 5 veces la hoja "Agregar track" | 61 | 2,8 ms | 11,6 ms | 23,1 ms | 2,9 % | 4,3 % |

El CPU es el porcentaje de un núcleo durante el recorrido. En los scrolls es más alto porque el recorrido dura menos y es casi todo dibujo; en las idas y vueltas, buena parte del tiempo es espera de red.

### Tiempo total por frame en los listados

El tiempo total incluye la GPU del emulador. Esta tabla usa solo las corridas en que la GPU respondió normal en ese recorrido (percentil 90 de GPU por debajo de 1 s):

| HU | Recorrido | Corridas válidas | Frames | Frames lentos | Percentil 50 | Percentil 90 | Percentil 99 |
|---|---|---:|---:|---:|---:|---:|---:|
| HU01 | Scroll del catálogo de álbumes | 4 de 5 | 161 | 13,9 % | 18 ms | 31 ms | 93 ms |
| HU03 | Scroll del listado de artistas | 4 de 5 | 196 | 10,8 % | 18 ms | 32 ms | 65 ms |
| HU05 | Scroll del listado de coleccionistas | 3 de 5 | 242 | 3,3 % | 18 ms | 25 ms | 32 ms |
| HU07 | Formulario de álbum con vista previa | 5 de 5 | 498 | 36,5 % | 17 ms | 34 ms | 150 ms |

En los detalles (HU02, HU04 y HU06) y en la hoja de HU08, el percentil 90 de GPU fue de 4,95 s en casi todas las corridas, así que su tiempo total por frame mide el emulador y no la app.

### Lectura

- **Los listados no tienen trabajo pesado en el hilo principal:** ningún frame pasa de 16 ms. Los frames lentos que aparecen en el tiempo total (3 % a 14 %) vienen del dibujo en la GPU del emulador, no del código de la app: el hilo principal termina cada frame en menos de 13 ms.
- **HU05 sigue siendo el listado más liviano** porque no carga imágenes: el avatar son iniciales. HU01 y HU03 tienen más frames lentos en el total porque dibujan portadas y fotos.
- **En los detalles (HU02, HU04 y HU06)** el percentil 50 del hilo principal es de 4 a 6 ms: la mayoría de los frames es rápida. El costo está en los primeros frames de cada pantalla, que inflan el _layout_ completo y agregan las filas de tracks, comentarios, premios, integrantes o colección; cada ida y vuelta además recrea el listado al volver. El script lee la jerarquía de vistas (`uiautomator dump`) mientras espera cada pantalla, lo que agrega trabajo en un emulador de 2 núcleos, así que estos valores son una cota superior. Para confirmar la causa hay que perfilar la apertura de un detalle con el CPU Profiler en un dispositivo físico ([#183](https://github.com/manuel-jose-marin/vinilos-mobile/issues/183)).
- **HU07** tiene el percentil 99 alto (107 ms): son unos pocos frames por recorrido, que coinciden con abrir el formulario, mostrar el teclado y cargar la vista previa de la portada. Fuera de esos momentos es fluido: percentil 90 de 4,7 ms y 2,5 % de frames sobre 16 ms. La espera de 400 ms antes de cargar la vista previa evita que haya una descarga por cada letra escrita.
- **HU08** es la pantalla nueva más liviana: la hoja solo tiene dos campos y dos botones.

## Arranque y memoria

| Momento | PSS total | _Heap_ de Java | _Heap_ nativo | Vistas vivas | Actividades |
|---|---:|---:|---:|---:|---:|
| Catálogo de álbumes cargado (HU01) | 72,7 MB | 13,3 MB | 22,1 MB | 141 | 1 |
| Después de 5 idas y vueltas al detalle de álbum (HU02) | 83,2 MB | 14,1 MB | 24,6 MB | 105 | 1 |
| Listado de artistas (HU03) | 87,6 MB | 18,3 MB | 23,7 MB | 119 | 1 |
| Después de 5 idas y vueltas al detalle de artista (HU04) | 96,2 MB | 18,9 MB | 31,3 MB | 95 | 1 |
| Listado de coleccionistas (HU05) | 89,3 MB | 18,2 MB | 24,9 MB | 120 | 1 |
| Después de 5 idas y vueltas al detalle de coleccionista (HU06) | 88,8 MB | 17,5 MB | 24,8 MB | 92 | 1 |
| Después de 5 veces el formulario de álbum (HU07) | 101,9 MB | 18,1 MB | 34,6 MB | 105 | 1 |
| Después de 5 veces la hoja "Agregar track" (HU08) | 98,8 MB | 19,4 MB | 29,5 MB | 77 | 1 |

Arranque en frío: **2 075 ms** (mediana; entre 1 723 y 2 278 ms en las 5 corridas).

### Lectura

- **No hay fugas de memoria.** Siempre hay una sola actividad viva, y en cada pantalla las vistas vivas bajan después de las idas y vueltas: el detalle de coleccionista, el formulario y la hoja se liberan al cerrarse. Esto confirma que liberar el binding y los adaptadores en `onDestroyView` funciona también en las pantallas nuevas.
- **El _heap_ de Java se mantiene entre 13 y 19 MB** en todo el recorrido de las ocho historias.
- **El PSS sube de 73 a 99 MB** a lo largo del recorrido, sobre todo en memoria nativa y gráfica: las portadas y fotos que Glide guarda en su caché para no volver a descargarlas. La caché tiene un tamaño máximo, así que no crece sin límite.
- **El pico de HU07 (102 MB, _heap_ nativo de 34,6 MB)** coincide con la vista previa de la portada, que se descarga y decodifica 5 veces. Después de salir del formulario, en la hoja de HU08, el _heap_ nativo baja a 29,5 MB: la vista previa se libera con `Glide.clear()` al destruir la vista del formulario.
- **Las mediciones de memoria son muy estables:** entre corridas, el PSS de cada momento varía menos de 1 MB.
- **El arranque de 2 s no se compara con los 473 ms del Sprint 2.** Se midió en otro equipo (Intel en lugar de Apple M4) y con la GPU del emulador lenta. En el Sprint 2, la medición con la GPU lenta también dio unos 2,5 s de arranque.

## Comparación con el Sprint 2

Las cifras no se comparan una a una porque el equipo, la arquitectura del emulador (x86_64 en lugar de arm64) y el estado de la GPU son distintos. Sí se comparan las conclusiones:

| Aspecto | Sprint 2 (HU01 a HU05) | Sprint 3 (HU01 a HU08) |
|---|---|---|
| Fugas de memoria | Ninguna: una actividad, vistas que bajan después de los detalles | Ninguna, también en las tres pantallas nuevas |
| _Heap_ de Java | 12 a 16 MB | 13 a 19 MB, con tres pantallas más |
| Hilo principal en los listados | Picos en HU01 (percentil 99 de 72 ms) | Sin frames sobre 16 ms en ningún listado |
| Hilo principal en los detalles | 6 % a 11 % de frames sobre 16 ms | 20 % a 24 % ([#183](https://github.com/manuel-jose-marin/vinilos-mobile/issues/183)) |

### Seguimiento de las recomendaciones del Sprint 2

| Recomendación del Sprint 2 | Estado |
|---|---|
| Iniciar el emulador con `-gpu host` para pruebas E2E y de desempeño | Cumplida. Aun así la GPU del emulador se volvió lenta, como en el Sprint 2 |
| Perfilar el scroll del catálogo y probar precargar portadas | No se hizo. En esta medición el catálogo no tiene frames sobre 16 ms en el hilo principal, así que deja de ser prioritaria |
| Reutilizar el adaptador del carrusel en el detalle de artista | Aplicada en el carrusel de favoritos del detalle de coleccionista ([[micro-optimizaciones del Sprint 3|Micro-optimizaciones-Sprint-3]]); en el detalle de artista sigue pendiente |
| Pedir al backend el autor dentro de cada comentario | No aplica: BackVynils es el backend dado por el curso |
| Volver a correr el análisis con HU06 a HU08 | Cumplida con este análisis |

## Recomendaciones

| Prioridad | Recomendación | Motivo |
|---|---|---|
| Media | Perfilar la apertura de los detalles con el CPU Profiler en un dispositivo físico ([#183](https://github.com/manuel-jose-marin/vinilos-mobile/issues/183)) | Es el único punto con frames lentos en el hilo principal, y el emulador no permite separar el costo de la app del costo de la medición |
| Media | Reutilizar el adaptador del carrusel en el detalle de artista, como ya se hace en el de coleccionista | Evita volver a crear el adaptador cada vez que llega el contenido |
| Baja | Repetir la medición en un emulador arm64 o en un dispositivo físico | Los resultados serían comparables con el Sprint 2 y no dependerían de la GPU del emulador |

## Cómo repetir la medición

```bash
# 1. Backend con los datos de prueba (una sola vez, sobre una base recién creada)
./scripts/seed-backvynils.sh
./scripts/perf/seed-perf-data.sh

# 2. Emulador con aceleración gráfica y la app instalada
emulator -avd <avd> -gpu host
./gradlew installDebug

# 3. Medir HU01 a HU08 (5 corridas)
python3 scripts/perf/measure.py --runs 5 --label develop --out resultados.json
```

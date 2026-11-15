# Análisis de desempeño del Sprint 2

**Fecha:** 15 de noviembre de 2026. **Tarea:** [#46](https://github.com/manuel-jose-marin/vinilos-mobile/issues/46).

Se ejecutó un Robo test en tres dispositivos Android físicos de Firebase Test Lab. Robo visitó HU01 a HU05 en los tres equipos. Las tres ejecuciones terminaron con resultado fallido por crashes de la aplicación: uno en Pixel 8, uno en Motorola G20 y tres en FUJITSU F-01L, todos con la misma firma en la limpieza de imágenes de Glide al salir del catálogo. No se reportaron ANR.

El primer dibujo de la Activity tardó entre 0,567 y 2,719 segundos. La CPU promedio estuvo entre 2,61 % y 4,01 %, y la memoria total promedio entre 71,63 y 195,84 MiB. Estas cifras describen una sola exploración por equipo, con cierres y relanzamientos; no constituyen una comparación controlada entre versiones de la app.

## Herramienta y método

Se usó **Firebase Test Lab, Robo, timeout de 5 minutos por dispositivo**, con métricas de rendimiento y video habilitados. El APK era **debug**, paquete `co.edu.uniandes.vinilos`, contra la API desplegada en `https://backvynils-grupo5.onrender.com/`. Las tres configuraciones se enviaron en una sola matriz, idioma español y orientación vertical. No hubo errores de infraestructura ni reintentos. Se utilizaron tres pruebas físicas del plan gratuito Spark, cuyo límite diario es de cinco. [Cuotas de Test Lab](https://firebase.google.com/docs/test-lab/usage-quotas-pricing).

- Proyecto: `vinilos-sprint-2`, Google Cloud CLI `593.0.0`.
- Matriz: `matrix-2p9yjmuxasdpd`, creada a las `19:42:54 UTC` (`14:42:54` en Colombia).
- [Resultados, videos, capturas y logs en Firebase](https://console.firebase.google.com/project/vinilos-sprint-2/testlab/histories/bh.83dd699809fc8f23/matrices/5886111069452124945). Requiere acceso al proyecto.
- Bucket: `gs://test-lab-yxdbwp2fcd3wm-wh292qdzxvftz/2026-11-15_14:42:47.267001_Wbrc/`.
- Antes de lanzar la matriz, `curl` a `/bands` respondió **HTTP 200** en **22,389 s**, a las `19:42:34 UTC`. Se esperó esa respuesta para despertar Render.
- El timeout solicitado fue de 300 s. Los mensajes del servicio informaron 313 s de ejecución para Pixel y Motorola, y 311 s para Fujitsu. Las series incluyen instrumentación previa y posterior y abarcan aproximadamente 324 a 339 s; no se recortaron a 300 s.

### Código y APK probados

Al compilar, los PR [#136](https://github.com/manuel-jose-marin/vinilos-mobile/pull/136) y [#137](https://github.com/manuel-jose-marin/vinilos-mobile/pull/137) todavía estaban abiertos. Se integraron localmente las ramas `feature/114-corrutinas-memoria` y `feature/113-lint-final` sobre `origin/develop` (`41e70aa109422b7bbb9b3a24d298706212e3ecd7`) únicamente para construir el APK.

```sh
./gradlew assembleDebug -Pvinilos.baseUrl=https://backvynils-grupo5.onrender.com/
```

La compilación terminó correctamente. El APK se conservó fuera del repositorio y se subió a Test Lab. Su SHA-256 es:

```text
c4cc9bb959008052de0eb1cae8035799b38f77a1b5d6b7844772bc5e7b6a55fb
```

El commit local de compilación fue `e1db6977f348f416db3565e26390d012fbfaf29d`. Después de compilar, GitHub integró #136 a las `19:18:37 UTC` y #137 a las `19:18:57 UTC`. Se verificó que el árbol completo compilado es idéntico al `develop` resultante, [933d2b8](https://github.com/manuel-jose-marin/vinilos-mobile/commit/933d2b8fe1b2b0c64f59a93673d63fe7e8e17e3b). La rama documental se recreó desde ese `origin/develop`, sin publicar los merges locales de compilación.

### Selección y ejecución

Se consultó el catálogo real con `gcloud firebase test android models list --filter='form=PHYSICAL' --format=json`. Se eligieron tres fabricantes, tres versiones de Android y capacidad alta disponible, cubriendo los intervalos API 34 o superior, API 30 a 33 y API 21 a 29.

```sh
gcloud firebase test android run \
  --project=vinilos-sprint-2 --type=robo \
  --app=vinilos-sprint-2-debug.apk \
  --device=model=shiba,version=34,locale=es,orientation=portrait \
  --device=model=java,version=30,locale=es,orientation=portrait \
  --device=model=F01L,version=27,locale=es,orientation=portrait \
  --timeout=5m --performance-metrics --record-video \
  --num-flaky-test-attempts=0 \
  --results-history-name=Vinilos-Sprint-2-issue-46 \
  --client-details=matrixLabel=Perfilamiento-Sprint-2-issue-46 \
  --async --format=json
```

## Dispositivos

Todos son teléfonos físicos. La RAM es la memoria total del equipo reportada por Tool Results, convertida de KiB a GiB, no la memoria de la aplicación.

| Fabricante | Modelo | ID Test Lab | Android | API | Pantalla | RAM reportada | Resultado |
|---|---|---|---|---:|---|---:|---|
| Google | Pixel 8 | `shiba` | 14 | 34 | 1080 × 2400 | 7,42 GiB | Fallido por crash |
| Motorola | G20 | `java` | 11 | 30 | 720 × 1600 | 3,40 GiB | Fallido por crash |
| FUJITSU | F-01L | `F01L` | 8.1.0 | 27 | 720 × 1280 | 2,78 GiB | Fallido por crash |

## Métricas comparativas

**N/D** significa que no hubo datos utilizables para calcular esa métrica, no que su valor fuera cero. CPU y memoria muestran promedio aritmético y máximo de las muestras registradas.

| Dispositivo | Arranque, s | CPU prom., % | CPU máx., % | Memoria prom., MiB | Memoria máx., MiB | Recibidos, bytes | Enviados, bytes | FPS / fotogramas lentos | Crashes | ANR |
|---|---:|---:|---:|---:|---:|---:|---:|---|---:|---:|
| Pixel 8 | 0,567 | 3,22 | 18,71 | 195,84 | 287,75 | N/D | N/D | N/D | 1 | 0 |
| Motorola G20 | 1,629 | 2,61 | 35,67 | 103,02 | 164,28 | N/D | N/D | N/D | 1 | 0 |
| FUJITSU F-01L | 2,719 | 4,01 | 36,00 | 71,63 | 110,90 | ≈ 10 179 132 | ≈ 294 427 | N/D | 3 | 0 |

### Fuentes y cálculo

Se consultaron `steps.get`, `perfSummary.get`, `perfSampleSeries.list` y `perfSampleSeries.samples.list` de la [Tool Results API](https://firebase.google.com/docs/test-lab/reference/toolresults/rest). Se descargaron del bucket los logs, las acciones de Robo, las capturas originales y los informes de crash.

- **Arranque:** tiempo hasta la primera visualización de `MainActivity`, no hasta la descarga completa del catálogo. En Motorola y Fujitsu procede de `appStartTime.initialDisplayTime` y se contrastó con `logcat`. En Pixel ese campo estaba vacío; se recuperaron los **567 ms** de la primera línea `Displayed ... MainActivity` del log. Se excluyeron los arranques posteriores a los crashes. Evidencia: [Pixel](./img/perfilamiento/shiba-arranque.txt), [Motorola](./img/perfilamiento/java-arranque.txt), [Fujitsu](./img/perfilamiento/F01L-arranque.txt). `fullyDrawnTime` estaba vacío en los tres equipos.
- **CPU:** serie `cpuTotal`, unidad `percent`, sin cambiar la normalización que entrega Test Lab. **Memoria:** `memoryTotal`, unidad `kibibyte`, dividida por 1024 para expresarla en MiB. Se conserva el nombre de la métrica del proveedor, sin equipararla a un heap dump ni atribuirla exclusivamente al heap de Java.
- **Promedios:** `sum(valor) / cantidad de muestras`; máximos sobre las mismas muestras. Se incluyen ceros, instrumentación, periodos sin proceso y relanzamientos. Los campos escalares `value` omitidos en muestras existentes se interpretan como el cero predeterminado de [ProtoJSON](https://protobuf.dev/programming-guides/json/). No se rellenan huecos temporales ni se interpreta una serie ausente como cero.
- **Red:** las series son tasas `networkReceived` y `networkSent` en bytes/s. Solo Fujitsu entregó una serie temporal utilizable. Los bytes de la tabla son **estimaciones**, calculadas como `sum((v[i] + v[i-1]) / 2 * (t[i] - t[i-1]))`. No se recibió un contador acumulado exacto. Pixel y Motorola solo tienen un marcador inicial por dirección, sin intervalo medible; no permiten deducir el tráfico.
- **Fluidez:** no se recibieron FPS ni fotogramas lentos. `graphicsStats` estaba vacío y no existían series de fotogramas. Los videos no sustituyen una medición de tiempos de renderizado. La disponibilidad depende del tipo de prueba y de la instrumentación; véase [análisis de resultados de Test Lab](https://firebase.google.com/docs/test-lab/android/analyzing-results).

| Dispositivo | Muestras CPU | Muestras memoria | Muestras red por dirección | Intervalo mediano CPU / memoria | Alcance temporal de CPU |
|---|---:|---:|---:|---|---:|
| Pixel 8 | 1578 | 634 | 1, no utilizable | 0,200 / 0,500 s | 325,574 s |
| Motorola G20 | 1566 | 626 | 1, no utilizable | 0,200 / 0,500 s | 324,190 s |
| FUJITSU F-01L | 1558 | 624 | 624 | 0,200 / 0,500 s | 339,027 s |

Se conservan las [muestras de Pixel](./img/perfilamiento/shiba-muestras.csv), [Motorola](./img/perfilamiento/java-muestras.csv) y [Fujitsu](./img/perfilamiento/F01L-muestras.csv), el [resumen numérico](./img/perfilamiento/resumen-metricas.json) y la [procedencia de cada artefacto](./img/perfilamiento/procedencia.json). [Este script](./img/perfilamiento/reproducir.py) regenera las gráficas y cálculos desde los CSV con Python 3 y Matplotlib. Las capturas se copiaron sin modificar y su SHA-256 está en la procedencia.

## Gráficas

Las gráficas de CPU comparten escala de 0 a 40 % y las de memoria de 0 a 320 MiB. El eje temporal empieza en la primera muestra de cada serie. La franja gris señala huecos superiores a dos segundos en la instrumentación; no son mediciones de actividad. Los ceros durante cierres y relanzamientos se conservan.

### Primera visualización

<img width="800" alt="Arranque: Pixel 0,567 s, Motorola 1,629 s y Fujitsu 2,719 s" src="./img/perfilamiento/arranque.png" />

### Google Pixel 8

<img width="900" alt="CPU del Pixel 8 durante la exploración de Robo" src="./img/perfilamiento/shiba-cpu.png" />

<img width="900" alt="Memoria total del Pixel 8 durante la exploración de Robo" src="./img/perfilamiento/shiba-memoria.png" />

No hay una gráfica de red válida para este equipo: cada dirección contiene solo un marcador inicial.

### Motorola G20

<img width="900" alt="CPU del Motorola G20 durante la exploración de Robo" src="./img/perfilamiento/java-cpu.png" />

<img width="900" alt="Memoria total del Motorola G20 durante la exploración de Robo" src="./img/perfilamiento/java-memoria.png" />

No hay una gráfica de red válida para este equipo: cada dirección contiene solo un marcador inicial.

### FUJITSU F-01L

<img width="900" alt="CPU del Fujitsu F-01L durante la exploración de Robo" src="./img/perfilamiento/F01L-cpu.png" />

<img width="900" alt="Memoria total del Fujitsu F-01L durante la exploración de Robo" src="./img/perfilamiento/F01L-memoria.png" />

<img width="900" alt="Tasa de recepción de red del Fujitsu F-01L" src="./img/perfilamiento/F01L-red-recibida.png" />

<img width="900" alt="Tasa de envío de red del Fujitsu F-01L" src="./img/perfilamiento/F01L-red-enviada.png" />

## Recorrido de Robo y capturas por HU

Se inspeccionaron visualmente las capturas de cada dispositivo y se contrastaron con `actions.json`. Robo visitó las cinco historias en los tres equipos. Esto acredita acceso a las pantallas, no la aprobación de todos sus criterios de aceptación. Algunas capturas muestran imágenes de sustitución, textos recortados o contenido desplazado; se conserva la evidencia original.

| Historia | Pixel 8: captura original | Motorola G20: captura original | FUJITSU F-01L: captura original |
|---|---|---|---|
| HU01: catálogo de álbumes | `artifacts/1.png` | `artifacts/1.png` | `artifacts/1.png` |
| HU02: detalle de álbum | `artifacts/12.png`, Buscando América | `artifacts/8.png`, Buscando América | `artifacts/19.png`, Buscando América |
| HU03: listado de artistas | `artifacts/4.png`, Todos | `artifacts/6.png`, Todos | `artifacts/3.png`, Todos |
| HU04: detalle de artista | `artifacts/11.png`, Freddie Mercury | `artifacts/7.png`, Freddie Mercury | `artifacts/16.png`, Freddie Mercury |
| HU05: listado de coleccionistas | `artifacts/3.png` | `artifacts/4.png` | `artifacts/2.png` |

Cada ruta original pertenece a `shiba-34-es-portrait`, `java-30-es-portrait` o `F01L-27-es-portrait` dentro del bucket, según su columna.

### HU01: catálogo de álbumes

| Google Pixel 8 | Motorola G20 | FUJITSU F-01L |
|---|---|---|
| <img width="220" alt="HU01 en Pixel 8" src="./img/perfilamiento/shiba-hu01.png" /> | <img width="220" alt="HU01 en Motorola G20" src="./img/perfilamiento/java-hu01.png" /> | <img width="220" alt="HU01 en Fujitsu F-01L" src="./img/perfilamiento/F01L-hu01.png" /> |

### HU02: detalle de álbum

| Google Pixel 8 | Motorola G20 | FUJITSU F-01L |
|---|---|---|
| <img width="220" alt="HU02 en Pixel 8" src="./img/perfilamiento/shiba-hu02.png" /> | <img width="220" alt="HU02 en Motorola G20" src="./img/perfilamiento/java-hu02.png" /> | <img width="220" alt="HU02 en Fujitsu F-01L" src="./img/perfilamiento/F01L-hu02.png" /> |

### HU03: listado de artistas

| Google Pixel 8 | Motorola G20 | FUJITSU F-01L |
|---|---|---|
| <img width="220" alt="HU03 en Pixel 8" src="./img/perfilamiento/shiba-hu03.png" /> | <img width="220" alt="HU03 en Motorola G20" src="./img/perfilamiento/java-hu03.png" /> | <img width="220" alt="HU03 en Fujitsu F-01L" src="./img/perfilamiento/F01L-hu03.png" /> |

### HU04: detalle de artista

| Google Pixel 8 | Motorola G20 | FUJITSU F-01L |
|---|---|---|
| <img width="220" alt="HU04 en Pixel 8" src="./img/perfilamiento/shiba-hu04.png" /> | <img width="220" alt="HU04 en Motorola G20" src="./img/perfilamiento/java-hu04.png" /> | <img width="220" alt="HU04 en Fujitsu F-01L" src="./img/perfilamiento/F01L-hu04.png" /> |

### HU05: listado de coleccionistas

| Google Pixel 8 | Motorola G20 | FUJITSU F-01L |
|---|---|---|
| <img width="220" alt="HU05 en Pixel 8" src="./img/perfilamiento/shiba-hu05.png" /> | <img width="220" alt="HU05 en Motorola G20" src="./img/perfilamiento/java-hu05.png" /> | <img width="220" alt="HU05 en Fujitsu F-01L" src="./img/perfilamiento/F01L-hu05.png" /> |

## Crashes y ANR

Tool Results marcó las tres ejecuciones con `failureDetail.crashed = true`. Los cinco archivos `data_app_crash_*_co_edu_uniandes_vinilos.txt` contienen la misma causa. Se abrió un issue por ocurrencia con label `bug`, milestone `Sprint 2`, pasos observados, traza completa y enlace al resultado. Son cinco registros de una misma firma, no cinco defectos independientes.

| Dispositivo | Ocurrencia | Hora del log del dispositivo | Acción BACK de Robo | Issue | Traza conservada |
|---|---:|---|---|---|---|
| Pixel 8 | 1 | 11:47:49 | 76, desde 231,816 s | [#138](https://github.com/manuel-jose-marin/vinilos-mobile/issues/138) | [Traza](./img/perfilamiento/shiba-crash-1.txt) |
| Motorola G20 | 1 | 11:45:55 | 29, desde 93,296 s | [#139](https://github.com/manuel-jose-marin/vinilos-mobile/issues/139) | [Traza](./img/perfilamiento/java-crash-1.txt) |
| FUJITSU F-01L | 1 | 11:46:56 | 24, desde 123,464 s | [#140](https://github.com/manuel-jose-marin/vinilos-mobile/issues/140) | [Traza](./img/perfilamiento/F01L-crash-1.txt) |
| FUJITSU F-01L | 2 | 11:47:23 | 31, desde 151,131 s | [#141](https://github.com/manuel-jose-marin/vinilos-mobile/issues/141) | [Traza](./img/perfilamiento/F01L-crash-2.txt) |
| FUJITSU F-01L | 3 | 11:48:50 | 51, desde 237,861 s | [#142](https://github.com/manuel-jose-marin/vinilos-mobile/issues/142) | [Traza](./img/perfilamiento/F01L-crash-3.txt) |

Las horas se conservan tal como aparecen en los logs del 15 de noviembre; no son hora de Colombia. Los segundos de la acción son relativos al registro de Robo, no al eje de las series de rendimiento. Se conservaron los extractos de acciones de [Pixel](./img/perfilamiento/shiba-acciones-crash.json), [Motorola](./img/perfilamiento/java-acciones-crash.json) y [Fujitsu](./img/perfilamiento/F01L-acciones-crash.json).

El patrón observado fue regresar al catálogo, pulsar Atrás para salir de `MainActivity` y fallar durante la destrucción de la pantalla. El fragmento retira el adaptador y este intenta obtener un gestor de Glide para una Activity ya destruida:

```text
java.lang.RuntimeException: Unable to destroy activity ... MainActivity
Caused by: java.lang.IllegalArgumentException:
  You cannot start a load for a destroyed activity
  at com.bumptech.glide.manager.RequestManagerRetriever.assertNotDestroyed(...:236)
  at com.bumptech.glide.Glide.with(Glide.java:634)
  at co.edu.uniandes.vinilos.ui.AlbumAdapter$AlbumViewHolder.clearImage(AlbumAdapter.kt:35)
  at co.edu.uniandes.vinilos.ui.AlbumAdapter.onViewRecycled(AlbumAdapter.kt:30)
  ...
  at co.edu.uniandes.vinilos.ui.AlbumListFragment.onDestroyView(AlbumListFragment.kt:70)
```

Es un extracto de la traza, no una traza completa. Los archivos enlazados y los issues incluyen el original. **No se reportaron ANR** en los resultados de Robo ni se encontraron registros `ANR in co.edu.uniandes.vinilos` en los logcat descargados. No se crearon issues de ANR.

## Análisis y relación con las prácticas del Sprint 2

### Arranque y diferencias entre dispositivos

Pixel mostró la primera Activity en 0,567 s; Motorola tardó aproximadamente 2,87 veces ese tiempo y Fujitsu 4,80 veces. El orden es compatible con diferencias de hardware y versión del sistema, pero una sola apertura por modelo no permite aislar su causa ni estimar variabilidad. La primera visualización tampoco mide cuándo terminó de responder la API o de cargar una portada.

Fujitsu tuvo la mayor CPU promedio, 4,01 %, y un pico de 36,00 %. Motorola tuvo la menor media, 2,61 %, pero un pico cercano, 35,67 %. Pixel presentó el pico menor, 18,71 %. Las medias bajas incluyen espera de red, pausas de Robo y reinicios, por lo que no demuestran ausencia de trabajo costoso en el hilo principal.

Pixel tuvo la mayor memoria promedio y máxima, 195,84 y 287,75 MiB. Motorola registró 103,02 y 164,28 MiB; Fujitsu, 71,63 y 110,90 MiB. La resolución, el sistema, las imágenes, las cachés y el recorrido son posibles factores, no causas comprobadas. Fujitsu sufrió tres reinicios, lo que también afecta su media. No hay heap dumps, medición de objetos retenidos ni una línea base anterior con el mismo recorrido; estos datos no bastan para declarar una fuga o atribuir una reducción a una optimización concreta.

### Corrutinas y conversión de JSON

La página [[ANR y memoria del Sprint 2|ANR-y-memoria-Sprint-2]] documenta `viewModelScope`, cancelación de peticiones de Volley y conversión de JSON a modelos con `withContext(Dispatchers.Default)`. El APK probado incluye esas prácticas de #136. Sacar la conversión del hilo principal y no bloquearlo esperando HTTP aborda el riesgo de ANR durante las cargas de HU01 a HU05.

El recorrido no produjo ANR reportados, pero no incluyó una comparación antes/después, trazas del hilo principal ni una prueba de estrés con respuestas grandes. Por tanto, el resultado es compatible con ese objetivo y no prueba por sí solo que todas las rutas estén libres de bloqueos. La latencia de Render puede mantener una pantalla cargando sin que eso constituya un ANR.

### Glide y ciclo de vida

La misma página describe Glide, reciclaje de filas y liberación de adaptadores en `onDestroyView` para reducir retenciones. La traza revela un problema concreto en la combinación de esas dos limpiezas: `AlbumAdapter.clearImage` invoca `Glide.with(view)` cuando la Activity ya está destruida. Que una operación se llame `clear` no evita que primero se intente obtener un gestor asociado a esa Activity.

La prioridad derivada de la evidencia es revisar el orden de destrucción y la asociación del gestor de imágenes al ciclo de vida, conservando la liberación de recursos. Después de corregirlo, conviene repetir el caso de Atrás desde HU01 y el recorrido en los tres sistemas. No se modificó código de la app durante este perfilamiento. Los tres cierres de Fujitsu muestran recurrencia durante esta exploración, no una tasa de fallos comparable entre modelos: Robo recorrió secuencias diferentes.

## Limitaciones y datos no disponibles

- **Dispositivos de laboratorio:** sus condiciones, conectividad, almacenamiento y procesos de fondo no representan toda la población de usuarios.
- **Recorrido automático de Robo:** visitó las cinco HU, pero las acciones y el tiempo por pantalla difieren entre equipos. No sustituye pruebas de aceptación ni asegura haber ejercitado todos los estados de carga, error y navegación.
- **API en Render:** el despertar tardó 22,389 s en la comprobación previa. La latencia posterior, las imágenes remotas y el estado compartido de los datos también influyen; no se midió cada petición por separado.
- **APK debug y una corrida:** no hay mediciones de release, repeticiones estadísticas ni línea base anterior. Los crashes y relanzamientos forman parte de los promedios presentados.
- **Red incompleta:** Pixel y Motorola no entregaron series utilizables. En Fujitsu solo se pudo estimar el volumen integrando tasas; faltan contadores acumulados exactos en los tres equipos.
- **Arranque parcial:** no hay tiempo hasta el contenido completamente dibujado (`fullyDrawnTime`). Pixel no entregó el campo de primer dibujo en Tool Results, aunque pudo recuperarse del logcat.
- **Sin FPS ni fotogramas lentos:** no se infieren del video, del uso de CPU ni de la ausencia de ANR.
- **Retención y acceso:** la consola y el bucket requieren permisos del proyecto y están sujetos a la retención del servicio. Las gráficas, muestras, trazas relevantes y capturas incluidas en esta página quedan versionadas con el reporte.

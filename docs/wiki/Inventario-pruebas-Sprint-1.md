# Inventario de pruebas · Sprint 1

Casos de prueba de **HU01 Consultar catálogo de álbumes** y **HU02 Consultar la información detallada de un álbum**, derivados de los criterios de aceptación de [#1](https://github.com/manuel-jose-marin/vinilos-mobile/issues/1) y [#2](https://github.com/manuel-jose-marin/vinilos-mobile/issues/2). Cada caso indica el criterio que cubre y si se ejecuta como prueba E2E manual o automatizada con Espresso. La sección [Pruebas que respaldan cada caso](#pruebas-que-respaldan-cada-caso) indica qué pruebas ya están escritas. Cómo se planean y ejecutan está en la [Estrategia de pruebas](Estrategia-pruebas-Sprint-1).

**Entorno:** emulador o dispositivo con Android 5.0 (API 21) o superior, animaciones del sistema desactivadas para Espresso, y BackVynils corriendo según [Backend BackVynils](Backend-BackVynils), con los datos de `scripts/seed-backvynils.sh`.

**Datos de prueba que usan los casos:**

| Dato | Origen | Casos |
|---|---|---|
| 12 álbumes, más de los que caben en una pantalla | Base inicial (4) y script (8) | HU01-01, HU01-06, HU01-08, HU02-01, HU02-07 |
| "Siembra": 3 tracks (uno de nombre largo) y 1 comentario | Script | HU02-02, HU02-03 |
| "Portada rota": URL de portada inválida | Script | HU01-05 |
| "Poeta del pueblo": sin tracks ni comentarios | Base inicial | HU02-04 |
| Catálogo vacío | Borrar los álbumes de la base (ver HU01-04) | HU01-04 |

## Resumen

| HU | Casos | Espresso | Manuales | Fuera del Sprint 1 |
|---|---|---|---|---|
| HU01 | 9 | 3 | 4 | 2 |
| HU02 | 7 | 4 | 2 | 1 |
| **Total** | **16** | **7** | **6** | **3** |

## HU01 · Consultar catálogo de álbumes

| ID | Criterio | Caso | Precondición | Pasos | Resultado esperado | Tipo |
|---|---|---|---|---|---|---|
| HU01-01 | 1 | Ver el catálogo de álbumes | El backend tiene álbumes | 1. Abrir la app en la sección Álbumes | Se ve una cuadrícula de 2 columnas; cada tarjeta muestra portada, nombre, artista, año y género | Espresso |
| HU01-02 | 2 | Ver tarjetas de carga | Conexión lenta (perfil de red lento en el emulador) | 1. Abrir la sección Álbumes | Mientras llega la respuesta se ven tarjetas de carga; al llegar los datos se reemplazan por los álbumes | Manual |
| HU01-03 | 3 | Error sin conexión y reintento | Modo avión activado | 1. Abrir la sección Álbumes<br>2. Desactivar el modo avión<br>3. Tocar "Reintentar" | Paso 1: se ve "No pudimos cargar los álbumes" y el botón "Reintentar". Paso 3: se carga el catálogo | Manual |
| HU01-04 | 4 | Catálogo vacío | El backend no tiene álbumes. La base inicial siempre trae 4, así que se borran con `docker compose exec db psql -U postgres -d vinyls -c "delete from performer_albums_album; delete from collector_album; delete from comment; delete from track; delete from album;"` y después se recrea la base | 1. Abrir la sección Álbumes | Se ve el mensaje de que no hay álbumes | Manual |
| HU01-05 | 5 | Portada que no carga | Un álbum con URL de portada inválida ("Portada rota") | 1. Abrir la sección Álbumes<br>2. Desplazarse hasta "Portada rota" | La tarjeta de ese álbum muestra la imagen genérica | Manual |
| HU01-06 | 6 | Abrir el detalle desde el catálogo | El backend tiene álbumes | 1. Abrir la sección Álbumes<br>2. Tocar la tarjeta de un álbum | Se abre el detalle de ese álbum | Espresso |
| HU01-07 | 7 | Abrir el formulario Crear álbum | Requiere HU07 | 1. Tocar "Nuevo álbum" | Se abre el formulario Crear álbum | Fuera del Sprint 1 (se valida con HU07 en el Sprint 3) |
| HU01-08 | 8 | Último álbum visible sobre el botón | El backend tiene más álbumes de los que caben en pantalla | 1. Abrir la sección Álbumes<br>2. Desplazarse hasta el final | El último álbum queda visible por encima del botón "Nuevo álbum" | Espresso |
| HU01-09 | 9 | Álbum nuevo en el catálogo | Requiere HU07 | 1. Crear un álbum<br>2. Volver al catálogo | El álbum nuevo aparece en el catálogo | Fuera del Sprint 1 (se valida con HU07 en el Sprint 3) |

## HU02 · Consultar la información detallada de un álbum

| ID | Criterio | Caso | Precondición | Pasos | Resultado esperado | Tipo |
|---|---|---|---|---|---|---|
| HU02-01 | 1 | Ver la ficha del álbum | El backend tiene álbumes | 1. En el catálogo, tocar un álbum | Se ven portada, nombre, artista, fecha de lanzamiento, género, sello y descripción del álbum tocado | Espresso |
| HU02-02 | 2 | Ver los tracks | Álbum con tracks, uno de ellos con nombre largo ("Siembra") | 1. Abrir el detalle del álbum | Se ve "Tracks · N" y cada track muestra número, nombre y duración; el nombre largo aparece completo, sin cortarse (ocupa las líneas que necesite) | Espresso |
| HU02-03 | 3 | Ver los comentarios | Álbum con comentarios ("Siembra") | 1. Abrir el detalle del álbum | Cada comentario muestra autor, calificación de 1 a 5 y texto; no hay opción para escribir comentarios | Espresso ⚠️ `GET /albums/{id}` no trae el autor del comentario; ver la [Estrategia de pruebas](Estrategia-pruebas-Sprint-1) |
| HU02-04 | 4 | Secciones vacías | Álbum sin tracks ni comentarios ("Poeta del pueblo") | 1. Abrir el detalle del álbum | Las secciones de tracks y comentarios muestran el mensaje de lista vacía | Manual |
| HU02-05 | 5 | Error sin conexión y reintento | Catálogo cargado; luego modo avión activado | 1. Tocar un álbum<br>2. Desactivar el modo avión<br>3. Tocar "Reintentar" | Paso 1: se ve el mensaje de error con "Reintentar". Paso 3: se carga el detalle | Manual |
| HU02-06 | 6 | Abrir el formulario Agregar track | Requiere HU08 | 1. En el detalle, tocar "Agregar track" | Se abre el formulario para agregar un track | Fuera del Sprint 1 (se valida con HU08 en el Sprint 3) |
| HU02-07 | 7 | Volver al catálogo | Detalle de un álbum abierto | 1. Tocar la flecha de regreso<br>2. Abrir otro detalle y tocar el botón atrás del sistema | En ambos casos se vuelve al catálogo | Espresso |

## Automatización

| Clase Espresso | Casos | Issue | Estado |
|---|---|---|---|
| `AlbumListTest` | HU01-01, HU01-06, HU01-08 | [#17](https://github.com/manuel-jose-marin/vinilos-mobile/issues/17) | Escrita, en el borrador [#82](https://github.com/manuel-jose-marin/vinilos-mobile/pull/82); la vista del listado ya está en `develop`, así que puede ejecutarse |
| `AlbumDetailTest` | HU02-01, HU02-02, HU02-03, HU02-07 | [#22](https://github.com/manuel-jose-marin/vinilos-mobile/issues/22) | Sin escribir; la vista del detalle ([#21](https://github.com/manuel-jose-marin/vinilos-mobile/issues/21)) ya está en develop |

Los casos de carga, error de red, lista vacía y portada inválida quedan como manuales porque dependen de condiciones de red o de datos del backend que las pruebas E2E contra BackVynils no controlan. La lógica detrás de esos estados sí está cubierta por pruebas unitarias (ver la tabla siguiente).

## Pruebas que respaldan cada caso

Pruebas automatizadas escritas para cada caso. Estado: ✅ en `develop`, 🔀 en un pull request abierto, 📝 sin escribir.

| Caso | Prueba Espresso | Pruebas unitarias |
|---|---|---|
| HU01-01 | `AlbumListTest.catalogShowsAlbumCardsInTwoColumns` 🔀 | `AlbumServiceTest.parseAlbum_readsAllFieldsAndRelations` ✅, `AlbumServiceTest.parseAlbums_keepsServerOrder` ✅, `AlbumCardTextTest` (4) ✅ |
| HU01-02 | | `AlbumListViewModelTest.init_showsLoadingUntilTheRepositoryResponds` ✅ |
| HU01-03 | | `AlbumListViewModelTest.loadAlbums_whenTheRequestFails_exposesError` ✅, `AlbumListViewModelTest.retry_afterAnError_loadsTheAlbumsAgain` ✅ |
| HU01-04 | | `AlbumListViewModelTest.loadAlbums_withNoAlbums_exposesEmpty` ✅, `AlbumServiceTest.parseAlbums_emptyArray_returnsEmptyList` ✅ |
| HU01-05 | | Ninguna: la imagen genérica la pone Glide en la vista y solo se verifica en pantalla |
| HU01-06 | `AlbumListTest.tappingAlbumOpensItsDetail` 🔀 | |
| HU01-08 | `AlbumListTest.lastAlbumIsVisibleAboveNewAlbumButton` 🔀 | |
| HU02-01 | `AlbumDetailTest` 📝 | `AlbumServiceTest.parseAlbum_readsAllFieldsAndRelations` ✅, `AlbumDetailViewModelTest.loadAlbum_requestsTheGivenAlbumAndExposesIt` ✅ |
| HU02-02 | `AlbumDetailTest` 📝 | `AlbumServiceTest.parseAlbum_readsAllFieldsAndRelations` ✅ (tracks y duración) |
| HU02-03 | `AlbumDetailTest` 📝 | `AlbumServiceTest.parseAlbum_readsAllFieldsAndRelations` ✅ (calificación; el autor no viene del API) |
| HU02-04 | | `AlbumServiceTest.parseAlbum_withoutTracksOrComments_returnsEmptyLists` ✅, `AlbumDetailViewModelTest.loadAlbum_withoutTracksOrComments_exposesContentWithEmptyLists` ✅ |
| HU02-05 | | `AlbumDetailViewModelTest.init_showsLoadingUntilTheRepositoryResponds` ✅, `AlbumDetailViewModelTest.loadAlbum_whenTheRequestFails_exposesError` ✅, `AlbumDetailViewModelTest.retry_afterAnError_loadsTheAlbumAgain` ✅ |
| HU02-07 | `AlbumDetailTest` 📝 | |

`NetworkServiceAdapterTest.buildUrl_joinsBaseAndPathWithSingleSlash` ✅ respalda todos los casos, porque verifica las URLs de `GET /albums` y `GET /albums/{id}`.

| Tipo | Escritas | Pendientes |
|---|---|---|
| Unitarias | 19, todas en `develop` | Las de la vista del detalle, si [#21](https://github.com/manuel-jose-marin/vinilos-mobile/issues/21) agrega lógica nueva |
| Espresso | 3 de 7 | Las 4 de `AlbumDetailTest` |

## Resultados

**Fecha de ejecución:** 26 de octubre de 2026 (America/Bogota, UTC−05:00).

**Versión evaluada:** `develop`, commit `a8524068235106956ee02c3cffcf66330e6b0c90`, compilación debug.

**Dispositivo:** emulador Pixel 7 (`Vinilos_Pixel_7_API_35`), Android 15 (API 35), x86_64, orientación vertical, 1080 × 2400 px, densidad 420 dpi y escala de fuente 1.0; las tres escalas de animación del sistema en 0.

**Backend:** BackVynils local con Docker y PostgreSQL 15, preparado según [Backend BackVynils](Backend-BackVynils). Se confirmó que `http://localhost:3000/albums` devuelve 12 álbumes de la base inicial y el script de datos; la app usa `http://10.0.2.2:3000/`. Los 12 álbumes se conservaron al terminar.

**Evidencia:** capturas PNG mediante `adb exec-out screencap -p`, dumps XML mediante `adb exec-out uiautomator dump /dev/tty`, respuestas del backend y reportes de pruebas en `pr-verification-sprint1/`, carpeta hermana del clon y fuera del repositorio. Los nombres citados en la tabla se refieren a esa carpeta; las capturas y los dumps se identifican por el ID del caso. Para la transición rápida de HU01-02 se usa la secuencia de capturas indicada. El reporte de Espresso se conserva en `AlbumListTest-report/index.html` y `AlbumListTest-results.xml`.

**Condiciones controladas:** HU01-02 se ejecutó con `adb emu network delay gprs` y `adb emu network speed gsm`; se capturaron las tarjetas de carga y su reemplazo por el catálogo, y también se probó desactivar temporalmente Wi-Fi para usar la red móvil. Después se restauraron `delay none`, `speed full` y Wi-Fi. En HU01-03 y HU02-05 funcionaron `adb shell cmd connectivity airplane-mode enable` y `disable`, por lo que no se detuvo la API. Para HU01-04 se sirvió un archivo `albums` con `[]` mediante `python3 -m http.server 8000`, se instaló con `-Pvinilos.baseUrl=http://10.0.2.2:8000/` y luego se reinstaló sin esa propiedad para volver al backend real; no se borraron datos.

| ID | Resultado | Evidencia: observación | Issue del defecto |
|---|---|---|---|
| HU01-01 | Pasa | `AlbumListTest.catalogShowsAlbumCardsInTwoColumns` pasó: cuadrícula de 2 columnas con portada, nombre, artista, año y género (`AlbumListTest-results.xml`, `HU01-01-catalogo.png`). | — |
| HU01-02 | Pasa | Se observaron cuatro tarjetas de carga y después su reemplazo por los álbumes (`HU01-02-secuencia-1.png` y `HU01-02-secuencia-2.png`). En una revisión posterior, el 2026-10-31, se vio que las tarjetas de carga quedaban pegadas entre sí. | [#105](https://github.com/manuel-jose-marin/vinilos-mobile/issues/105), severidad baja |
| HU01-03 | Pasa | Con modo avión apareció «No pudimos cargar los álbumes» y «Reintentar»; al restablecer la red y tocarlo se cargó el catálogo (`HU01-03-sin-conexion.png` y `HU01-03-reintento.png`). | — |
| HU01-04 | Pasa | La respuesta `[]` del servidor temporal mostró «Aún no hay álbumes en el catálogo» (`HU01-04-vacio.png`). | — |
| HU01-05 | Pasa | La tarjeta «Portada rota», cuya URL es inválida, mostró la imagen genérica de un disco (`HU01-05-portada-rota.png`). | — |
| HU01-06 | Pasa | `AlbumListTest.tappingAlbumOpensItsDetail` pasó: tocar una tarjeta abrió el detalle del álbum seleccionado (`AlbumListTest-results.xml`). | — |
| HU01-07 | No aplica (Sprint 3) | No ejecutado: el formulario Crear álbum requiere HU07 y está fuera del Sprint 1. | — |
| HU01-08 | Pasa | `AlbumListTest.lastAlbumIsVisibleAboveNewAlbumButton` pasó: el último álbum quedó completamente por encima de «Nuevo álbum» (`AlbumListTest-results.xml`). | — |
| HU01-09 | No aplica (Sprint 3) | No ejecutado: crear un álbum y comprobar su incorporación requiere HU07, del Sprint 3. | — |
| HU02-01 | Pasa (manual; automatización pendiente en #22) | «Buscando América» mostró portada, nombre, Rubén Blades Bellido de Luna, «1 de agosto de 1984», Salsa, Elektra y la descripción del backend (`HU02-01-ficha.png`). | — |
| HU02-02 | Pasa | Re-ejecutado el 2026-10-26 tras la corrección del PR #96; el nombre largo se ve completo (`pr96-siembra.png`). | [#94](https://github.com/manuel-jose-marin/vinilos-mobile/issues/94), corregido en [PR #96](https://github.com/manuel-jose-marin/vinilos-mobile/pull/96) |
| HU02-03 | Pasa (manual; automatización pendiente en #22) | «Siembra» mostró a Manolo Bellon, calificación 4/5 y «Un clásico de la salsa.», sin campo ni botón para escribir comentarios (`HU02-02-03-siembra.png` y `HU02-03-comentario-final.xml`). | — |
| HU02-04 | Pasa | «Poeta del pueblo» mostró «Tracks · 0», «Comentarios · 0» y los dos mensajes de secciones vacías (`HU02-04-secciones-vacias.png`). | — |
| HU02-05 | Pasa | Tras cargar el catálogo y activar modo avión, abrir un álbum mostró «No pudimos cargar el álbum» y «Reintentar»; al restablecer la red y tocarlo se cargó el detalle (`HU02-05-sin-conexion.png` y `HU02-05-reintento.png`). | — |
| HU02-06 | No aplica (Sprint 3) | No ejecutado: el formulario Agregar track requiere HU08 y está fuera del Sprint 1. | — |
| HU02-07 | Pasa (manual; automatización pendiente en #22) | La flecha desde «Buscando América» y `KEYCODE_BACK` desde otro detalle («Poeta del pueblo») regresaron al catálogo (`HU02-07-flecha.png` y `HU02-07-atras-sistema.png`). | — |

**Resumen:** 16 casos registrados: **13 pasan, 0 fallan y 3 no aplican (Sprint 3)**. Se ejecutaron los 13 casos aplicables: 3 con Espresso y 10 manualmente; ninguno quedó sin ejecutar por bloqueo. `./gradlew connectedDebugAndroidTest` terminó correctamente con las 3 pruebas de `AlbumListTest`, sin fallos, errores ni omisiones. Los cuatro casos previstos para `AlbumDetailTest` se ejecutaron manualmente porque su automatización sigue pendiente en [#22](https://github.com/manuel-jose-marin/vinilos-mobile/issues/22).

**Defecto reportado:** [#94 — HU02-02 trunca el nombre largo del track](https://github.com/manuel-jose-marin/vinilos-mobile/issues/94), severidad media, etiqueta `bug` y milestone **Sprint 1**. No se modificó código de la app.

**Defecto reportado después de la ejecución:** [#105 — HU01-02 las tarjetas de carga del listado quedan pegadas](https://github.com/manuel-jose-marin/vinilos-mobile/issues/105), severidad baja, etiquetas `bug` y `HU01` y milestone **Sprint 1**. Se encontró el 2026-10-31 al probar el APK contra la API de Render. El criterio se cumple porque las tarjetas de carga sí aparecen, pero no tienen la separación de la cuadrícula: los `<include>` reemplazan el margen de la tarjeta. La ausencia de artista y la portada reutilizada de «Siembra» corresponden al script de datos y no se reportaron como defectos.

### Compatibilidad con la versión mínima

**Fecha de ejecución:** 28 de octubre de 2026 (America/Bogota, UTC−05:00).

**Versión evaluada:** `develop`, commit `a417dc5e124543f7510c0320ab5c1019ae36b8e3`, compilación debug instalada con `./gradlew installDebug`. No se modificó código de la app.

**Dispositivo:** emulador Pixel, AVD `Vinilos_Pixel_API_21`, **Android 5.0.2 (API 21)**, x86_64, orientación vertical, 1080 × 1920 px y densidad 420 dpi. El equipo es Intel (`uname -m`: `x86_64`); se instaló la imagen nativa `system-images;android-21;google_apis;x86_64`, revisión 32. No fue necesario usar una API superior. Se confirmó `sys.boot_completed=1` y `ro.build.version.sdk=21`; las tres escalas de animación estaban en 0.

**Backend y condiciones:** BackVynils se levantó con Docker y la configuración documentada; `/albums` devolvió los 12 álbumes y los datos requeridos, ya sembrados, por lo que no se repitió el seed. Para HU01-02 se usaron `network delay gprs` y `network speed gsm`, y una pausa breve del contenedor de la API durante la captura para mantener pendiente la respuesta; al reanudarlo, las tarjetas de carga se reemplazaron por el catálogo. Después se restauraron `delay none` y `speed full`. Como `cmd connectivity` no existe en API 21, para HU01-03 y HU02-05 se detuvo únicamente `backvynils-web-1`, se comprobó el error, se arrancó de nuevo y se tocó «Reintentar». Para HU01-04 se sirvió `[]` en el puerto 8000 con `python3 -m http.server`, instalando con `-Pvinilos.baseUrl=http://10.0.2.2:8000/`. Al terminar se cerró ese servidor y se reinstaló la app con la URL predeterminada; BackVynils quedó arriba con los mismos 12 álbumes.

**Evidencia:** `pr-verification-sprint1/android-5/`, fuera del repositorio, junto a la ejecución anterior. Contiene capturas PNG, dumps XML, respuestas del backend, logs y `manual-results.json`. Las capturas se obtuvieron con `adb exec-out screencap -p`. En esta imagen, `adb exec-out uiautomator dump /dev/tty` no entrega el XML; se guardó con `adb shell uiautomator dump /data/local/tmp/vinilos-window.xml` y se recuperó con `adb exec-out cat`. El dumper sustituye algunos caracteres acentuados por `?`; se cotejaron con las capturas, donde se muestran correctamente. Cada nombre de evidencia de la tabla tiene archivos `.png` y `.xml`.

| Caso | Resultado manual | Observación y evidencia |
|---|---|---|
| HU01-01 | Pasa | Cuadrícula de dos columnas con portada, nombre, artista, año y género (`HU01-01-catalogo`). |
| HU01-02 | Pasa | Cuatro tarjetas de carga, reemplazadas por los álbumes al llegar la respuesta (`HU01-02-cargando`, `HU01-02-catalogo`). |
| HU01-03 | Pasa | Error del catálogo y recuperación al tocar «Reintentar» (`HU01-03-sin-conexion`, `HU01-03-reintento`). |
| HU01-04 | Pasa | «Aún no hay álbumes en el catálogo» ante la respuesta `[]` (`HU01-04-vacio`). |
| HU01-05 | Pasa | «Portada rota» muestra la imagen genérica del disco (`HU01-05-portada-rota`). |
| HU01-06 | Pasa | Tocar «Buscando América» abre su detalle (`HU01-06-HU02-01-ficha`). |
| HU01-08 | Pasa | La última tarjeta termina en y=1332, por encima de «Nuevo álbum», que empieza en y=1395 (`HU01-08-ultimo-album`). |
| HU02-01 | Pasa | La ficha muestra portada, nombre, artista, fecha, género, sello y descripción de «Buscando América» (`HU01-06-HU02-01-ficha`). |
| HU02-02 | Pasa | «Tracks · 3», números, nombres y duraciones; el nombre largo de «Pedro Navaja» se ve completo en tres líneas (`HU02-02-03-siembra`). |
| HU02-03 | Pasa | Manolo Bellon, calificación 4/5 y «Un clásico de la salsa.», sin opción para escribir comentarios (`HU02-02-03-siembra`). |
| HU02-04 | Pasa | «Poeta del pueblo» muestra los dos contadores en 0 y los mensajes de tracks y comentarios vacíos (`HU02-04-secciones-vacias`). |
| HU02-05 | Pasa | Error del detalle y recuperación al tocar «Reintentar» (`HU02-05-sin-conexion`, `HU02-05-reintento`). |
| HU02-07 | Pasa | La flecha desde «Buscando América» y Atrás desde «Poeta del pueblo» vuelven al catálogo (`HU02-07-flecha`, `HU02-07-atras-sistema`). |

**Resultado manual:** **13 pasan, 0 fallan y 0 bloqueados**. HU01-07, HU01-09 y HU02-06 no se ejecutaron porque no aplican al Sprint 1. Los 13 casos aplicables se verificaron manualmente; tres de ellos también se ejecutaron con Espresso.

**Espresso:** `./gradlew connectedDebugAndroidTest` terminó con `BUILD SUCCESSFUL`: **3 pruebas de `AlbumListTest`, 0 fallos, 0 errores y 0 omisiones**, en Android 5.0.2. Pasaron `catalogShowsAlbumCardsInTwoColumns`, `tappingAlbumOpensItsDetail` y `lastAlbumIsVisibleAboveNewAlbumButton`. El reporte está en `Espresso-report/index.html`, el XML en `Espresso-results/` y la salida de Gradle en `Espresso-gradle.log`. Los cuatro casos de detalle cuya automatización sigue pendiente en #22 se comprobaron manualmente.

**Logcat y defectos:** se limpió logcat antes de instalar y se revisaron la captura continua y `adb logcat -d` al finalizar (`logcat-complete.txt`, `logcat-final.txt`, `logcat-app-errors.txt` y `logcat-review.json`). **No hubo crashes de la app ni `FATAL EXCEPTION`**. Se observaron mensajes gráficos `eglCodecCommon` del emulador y un `NoSuchMethodError` capturado en `AndroidXTracer.enableTracing` durante Espresso, al buscar `androidx.tracing.Trace.forceEnableAppTracing()`; este último afecta al trazado de la instrumentación, sin abortar ninguna prueba. No se encontraron casos funcionales fallidos. **Issues creados: ninguno**.

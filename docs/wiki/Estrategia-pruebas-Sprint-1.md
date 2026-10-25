# Estrategia de pruebas · Sprint 1

Cómo probamos **HU01 Consultar catálogo de álbumes** y **HU02 Consultar la información detallada de un álbum** antes de cerrar el sprint. Los casos concretos están en el [Inventario de pruebas](Inventario-pruebas-Sprint-1); esta página define qué se prueba, con qué, dónde y cuándo se da por terminado.

## Objetivo

Comprobar que los criterios de aceptación de [#1](https://github.com/manuel-jose-marin/vinilos-mobile/issues/1) y [#2](https://github.com/manuel-jose-marin/vinilos-mobile/issues/2) se cumplen en la app contra BackVynils, en las versiones de Android que soporta el proyecto, y que la lógica de cada capa se puede verificar sin red.

## Alcance

| Incluido | Fuera del Sprint 1 |
|---|---|
| Listado de álbumes: cuadrícula, estados de carga, vacío y error, reintento, portada genérica y apertura del detalle | "Nuevo álbum" y el álbum creado en el catálogo (HU01 criterios 7 y 9): se prueban con HU07 en el Sprint 3 |
| Detalle de álbum: ficha, tracks, comentarios, secciones vacías, error y reintento, regreso | "Agregar track" (HU02 criterio 6): se prueba con HU08 en el Sprint 3 |
| Parseo de JSON, construcción de URLs y estados de los ViewModels | Pruebas de rendimiento, seguridad y usabilidad con usuarios |

## Niveles y tipos de prueba

| Nivel | Qué verifica | Herramienta | Dónde vive | Quién |
|---|---|---|---|---|
| Unitarias | Parseo de álbumes, URLs del Service Adapter, estados de `AlbumListViewModel` y `AlbumDetailViewModel` (carga, contenido, vacío, error y reintento) y textos de las tarjetas | JUnit 4, `kotlinx-coroutines-test`, `InstantTaskExecutorRule` y servicios falsos | `app/src/test` | Quien implementa cada capa, en el mismo PR |
| E2E automatizadas | Recorridos de usuario estables contra BackVynils: catálogo en 2 columnas, abrir el detalle, último álbum sobre "Nuevo álbum", ficha, tracks, comentarios y regreso | Espresso | `app/src/androidTest` (`AlbumListTest`, `AlbumDetailTest`) | #17 (HU01) y #22 (HU02) |
| E2E manuales | Casos que dependen de la red o de datos que Espresso no controla: carga lenta, sin conexión y reintento, catálogo vacío, portada inválida y secciones vacías | Emulador con perfil de red y modo avión | [Inventario de pruebas](Inventario-pruebas-Sprint-1) | Responsable de pruebas del sprint |
| Exploratorias | Rotación de pantalla, textos largos, toques rápidos repetidos y navegación atrás desde cada pantalla | Emulador y dispositivo físico | Notas en el reporte de resultados | Todo el equipo, 30 minutos antes del cierre |

Las pruebas unitarias no tocan la red: el repositorio y los ViewModels reciben un `AlbumService` falso. Las E2E sí usan BackVynils, por eso solo se automatizan los casos cuyo resultado no depende del estado de la red.

## Técnicas

- **Basadas en criterios de aceptación:** cada criterio en alcance tiene al menos un caso en el inventario.
- **Partición de estados:** cada pantalla tiene un estado sellado (`Loading`, `Content`, `Empty`, `Error`). Hay una prueba unitaria por estado y por transición de reintento.
- **Valores límite en los datos:** lista vacía, álbum sin tracks ni comentarios, nombre de track largo, portada sin URL o inválida, álbum inexistente (`404`).

## Ambiente

| Elemento | Definición |
|---|---|
| Backend | BackVynils local con Docker, según [Backend BackVynils](Backend-BackVynils) |
| Datos | Base inicial más `scripts/seed-backvynils.sh`: 12 álbumes, uno con tracks (uno de nombre largo) y comentarios, uno sin tracks ni comentarios y uno con portada inválida |
| Versiones de Android | API 21 (Android 5.0, `minSdk`), API 29 (Android 10) y API 35 (Android 15, `targetSdk`) en emuladores Pixel; al menos un dispositivo físico en la versión que tenga el equipo |
| Configuración para Espresso | Animaciones del sistema desactivadas (escala de ventana, transición y animador en 0) |
| JDK y compilación | JDK 17; `./gradlew testDebugUnitTest` y `./gradlew connectedDebugAndroidTest` |

Las tres versiones cubren el mínimo soportado, una versión intermedia y la versión objetivo. La E2E completa se corre en las tres antes de pasar a `main`, como pide la [regla de GitFlow](Reglas-de-trabajo).

## Criterios de entrada y salida

| | Criterio |
|---|---|
| Entrada a un PR | Compila y pasan las pruebas unitarias locales; las clases nuevas de `viewmodels`, `repositories` y `network` traen sus pruebas unitarias |
| Entrada a la ejecución E2E | HU01 y HU02 integradas en `develop`; BackVynils corriendo con los datos de prueba |
| Salida del sprint | 100 % de pruebas unitarias en verde; todos los casos del inventario en alcance ejecutados; sin defectos de severidad alta abiertos; resultados registrados en el inventario |

## Gestión de defectos

Cada defecto se reporta como issue con la etiqueta `bug`, la etiqueta de la historia (`HU01` o `HU02`) y el milestone del sprint. El cuerpo incluye:

- Caso del inventario (por ejemplo `HU01-03`) o "exploratoria".
- Pasos para reproducirlo, resultado esperado y resultado obtenido.
- Versión de Android y dispositivo o emulador.
- Severidad y evidencia (captura o video).

| Severidad | Significado | Tratamiento |
|---|---|---|
| Alta | La app se cierra, o un criterio de aceptación no se cumple | Bloquea el cierre del sprint; se corrige antes de la entrega |
| Media | El criterio se cumple con errores visibles (texto cortado, estado incorrecto por un momento) | Se corrige en el sprint si hay capacidad; si no, pasa al backlog |
| Baja | Detalle visual o de textos que no afecta el uso | Pasa al backlog |

Quien corrige el defecto enlaza el issue en el PR (`Closes #N`) y el responsable de pruebas vuelve a ejecutar el caso antes de cerrarlo.

## Cronograma

| Cuándo | Actividad |
|---|---|
| Durante el sprint, en cada PR | Pruebas unitarias del código nuevo; revisión por pares |
| Cuando HU01 y HU02 estén en `develop` | Ejecución de Espresso en las tres versiones de Android y de los casos manuales |
| Sábado 2026-10-31 | Pruebas exploratorias, reporte de defectos y corrección de los de severidad alta |
| Domingo 2026-11-01 | Resultados en el inventario y cierre del sprint |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Espresso no sincroniza las peticiones de Volley y las pruebas fallan de forma intermitente | Esperas explícitas con tiempo máximo (`waitUntilDisplayed` de #17) en lugar de pausas fijas |
| Los datos del backend cambian entre ejecuciones (por ejemplo, al probar otras historias) | Base recreada con `docker compose down && rm -rf pgdata && docker compose up -d` y el script de datos antes de cada ejecución completa |
| Librerías recientes que no soportan API 21 | Versiones fijadas en `libs.versions.toml` y ejecución en el emulador API 21 |
| `GET /albums/{id}` no trae el autor de los comentarios que pide HU02 (criterio 3) | Se resuelve en la implementación del detalle (#19 y #21) y se valida en el caso HU02-03 |

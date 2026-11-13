# Micro-optimizaciones · Sprint 2

Micro-optimizaciones aplicadas a partir del análisis estático con **Android Lint** (`./gradlew lintDebug`, configuración por defecto de AGP 8.7.3). El reporte queda en `app/build/reports/lint-results-debug.html`.

## Primera pasada (HU01, HU02 y estructura base)

Lint se ejecutó sobre `develop` con HU01 y HU02 integradas, antes de integrar HU03 a HU05.

| | Errores | Advertencias | De código y recursos | De versiones de dependencias |
|---|---|---|---|---|
| Antes | 0 | 56 | 11 | 45 |
| Después | 0 | 48 | 3 | 45 |

### Hallazgos corregidos

| Regla de Lint | Archivo | Problema | Cambio aplicado |
|---|---|---|---|
| `Overdraw` (rendimiento) | `activity_main.xml` | La raíz pintaba `?attr/colorSurface` encima del fondo de la ventana, que el tema ya pinta con el mismo color (`android:colorBackground`). Cada píxel se dibujaba dos veces | Se quitó `android:background` de la raíz |
| `Overdraw` (rendimiento) | `fragment_album_list.xml` | Igual que el anterior: el fragmento repintaba el fondo de la ventana | Se quitó `android:background` de la raíz |
| `Overdraw` (rendimiento) | `fragment_album_detail.xml` | Igual que el anterior | Se quitó `android:background` de la raíz |
| `DisableBaselineAlignment` (rendimiento) | `fragment_album_list.xml` (2 filas de tarjetas de carga) | Los `LinearLayout` horizontales con pesos calculaban la alineación por línea base de sus hijos, aunque las tarjetas no la necesitan | `android:baselineAligned="false"` en las dos filas |
| `SetTextI18n` | `AlbumDetailFragment.kt` | El número de cada track se armaba con `toString()`, sin formato según el idioma | Recurso `album_track_number` con `getString(R.string.album_track_number, n)` |
| `UnusedAttribute` | `AndroidManifest.xml` | `usesCleartextTraffic` solo existe desde API 23 y `minSdk` es 21 | `tools:targetApi="23"`. En API 21 y 22 el tráfico HTTP ya está permitido por defecto, así que el comportamiento no cambia |

### Optimización adicional en código

| Archivo | Problema | Cambio aplicado |
|---|---|---|
| `AlbumDetailText.kt` | `releaseDate()` creaba dos `SimpleDateFormat` y un `TimeZone` en cada llamada | Los formateadores se crean una sola vez como propiedades del objeto. Se usan solo desde el hilo principal, porque `SimpleDateFormat` no es seguro entre hilos |

### Hallazgos no corregidos

| Regla de Lint | Cantidad | Motivo |
|---|---|---|
| `GradleDependency`, `AndroidGradlePluginVersion` | 45 | Las versiones de AndroidX están fijadas en `gradle/libs.versions.toml` para mantener `minSdk 21` (Android 5.0); varias versiones más recientes exigen `minSdk 23` |
| `Overdraw` en `fragment_artist_list.xml` y `fragment_collector_list.xml` | 2 | Son las pantallas temporales de HU03 y HU05, que se reemplazan en este sprint. Los layouts nuevos no deben pintar fondo en la raíz |
| `MissingApplicationIcon` | 1 | No afecta el desempeño; queda pendiente el ícono de la app |

### Verificación

- `./gradlew testDebugUnitTest lintDebug assembleDebug` termina sin errores.
- Las pantallas se ven igual: el color de fondo que se quitó es el mismo que pinta la ventana.

## Segunda pasada (HU03, HU04 y HU05)

Lint se ejecutó sobre `develop` con los servicios, repositorios y ViewModels de HU03, HU04 y HU05, la vista del listado de artistas y los layouts de las tres pantallas.

| | Errores | Advertencias | De código y recursos | De versiones de dependencias |
|---|---|---|---|---|
| Antes | 0 | 55 | 10 | 45 |
| Después | 0 | 52 | 7 | 45 |

### Hallazgos corregidos

| Regla de Lint | Archivo | Problema | Cambio aplicado |
|---|---|---|---|
| `Overdraw` (rendimiento) | `fragment_artist_list.xml` | La raíz repintaba con `?attr/colorSurface` el fondo que el tema ya pinta en la ventana | Se quitó `android:background` de la raíz |
| `Overdraw` (rendimiento) | `fragment_artist_detail.xml` | Igual que el anterior | Se quitó `android:background` de la raíz |
| `UnusedResources` | `strings.xml` | `artists_title` no se usa: la barra del listado usa `nav_artists` | Se eliminó el texto |
| `PluralsCandidate` | `strings.xml`, `ArtistAdapter.kt` | "N álbumes" se armaba con dos textos y un `when` | Recurso `plurals` `artist_albums_count` con `getQuantityString` (se corrigió al implementar la vista del listado de artistas, #31) |

El `Overdraw` de `fragment_collector_list.xml` de la primera pasada ya no aparece: el layout definitivo de HU05 (#38) no pinta fondo en la raíz.

### Hallazgos no corregidos

| Regla de Lint | Cantidad | Motivo |
|---|---|---|
| `GradleDependency`, `AndroidGradlePluginVersion` | 45 | Igual que en la primera pasada: versiones fijadas para mantener `minSdk 21` |
| `UnusedResources` | 5 | `item_artist_prize.xml`, `item_artist_member.xml`, `ic_prize.xml`, `bg_prize_icon.xml` y el plural `collector_albums_count` son de las vistas del detalle de artista (#36) y del listado de coleccionistas (#41), que los usan al integrarse |
| `Overdraw` en `item_artist_album.xml` | 1 | Falso positivo: `?attr/selectableItemBackground` solo pinta el efecto al tocar. Pasarlo a `android:foreground` exige API 23 en un `LinearLayout` y la app soporta desde API 21 |
| `MissingApplicationIcon` | 1 | No afecta el desempeño; queda pendiente el ícono de la app |

### Verificación

- `./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` termina sin errores.
- Antes del release se vuelve a ejecutar Lint con las vistas del detalle de artista y del listado de coleccionistas integradas.

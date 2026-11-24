# Micro-optimizaciones · Sprint 3

Micro-optimizaciones de las pantallas nuevas del Sprint 3: el detalle de coleccionista (HU06), el formulario de creación de álbum (HU07) y la hoja "Agregar track" (HU08). Las de HU01 a HU05 siguen vigentes y están en las [[micro-optimizaciones del Sprint 2|Micro-optimizaciones-Sprint-2]]. Las prácticas de memoria, ANRs y accesibilidad están en [[buenas prácticas del Sprint 3|Buenas-practicas-Sprint-3]].

## Optimizaciones en el código

| # | Historia | Problema que evita | Cambio | Dónde |
|---|---|---|---|---|
| 1 | HU06 | Completar el artista de cada álbum de la colección con un `GET /albums/{id}` por álbum haría N peticiones seguidas | Un solo `GET /albums`, en paralelo con el coleccionista y su colección (`async`), y el cruce por id en memoria | `CollectorRepository.getCollectorDetail` |
| 2 | HU06 | El carrusel de favoritos creaba un adaptador nuevo cada vez que llegaba el contenido (por ejemplo, al volver del detalle de un artista), y cada `submitList()` podía volver a medir la lista | Un solo adaptador por vista, creado en `onViewCreated`, al que solo se le pasa la lista nueva; `setHasFixedSize(true)` porque la altura del carrusel no depende de su contenido | `CollectorDetailFragment` |
| 3 | HU07 | La vista previa de la portada descargaba una imagen por cada letra escrita en la URL | Espera 400 ms a que el usuario deje de escribir (`PREVIEW_DELAY_MS`) y cancela la espera anterior; solo carga la imagen si la URL ya es válida | `CreateAlbumFragment.setUpCoverPreview` |
| 4 | HU07 | Si el usuario sale mientras carga la vista previa, Glide terminaría la descarga para una vista que ya no existe | `Glide.clear()` sobre la vista previa en `onDestroyView`, y la espera corre en `viewLifecycleOwner.lifecycleScope`, que se cancela con la vista | `CreateAlbumFragment` |
| 5 | HU07 y HU08 | Un formulario inválido haría un `POST` que el servidor rechazaría | La validación corre en memoria antes de enviar (`AlbumFormValidator`, `TrackFormValidator`); si hay errores, no se hace ninguna petición | `CreateAlbumViewModel.save`, `AddTrackViewModel.save` |
| 6 | HU07 y HU08 | Un doble toque en "Crear álbum" o "Agregar" enviaría dos `POST` y crearía dos registros | `save()` no hace nada mientras el estado es `Saving` o ya se guardó, y la vista deshabilita el botón | `CreateAlbumViewModel`, `AddTrackViewModel` |
| 7 | HU08 | Después de agregar un track, volver a pedir el álbum (`GET /albums/{id}`) repetiría una petición y mostraría otra vez la carga | La respuesta del `POST` ya trae el track con su `id`; el detalle lo suma a la lista que tiene en memoria | `AlbumDetailViewModel.onTrackAdded` |
| 8 | HU06 a HU08 | Convertir JSON en el hilo principal bloquea la interfaz | Los cuatro métodos nuevos de los servicios (`getCollector`, `getCollectorAlbums`, `createAlbum` y `addTrack`) usan `parseOffMainThread`, igual que los del Sprint 2 | `CollectorService`, `AlbumService` |

## Pasada de Android Lint

`./gradlew lintDebug` sobre `develop` con las ocho historias integradas, antes y después del cambio 2:

| | Errores | Advertencias | De código y recursos | De versiones de dependencias |
|---|---:|---:|---:|---:|
| Antes | 0 | 48 | 3 | 45 |
| Después | 0 | 48 | 3 | 45 |

El cambio 2 no corresponde a una regla de Lint, así que no cambia el conteo. Las tres advertencias de código y recursos que quedan se revisaron:

| Regla de Lint | Cantidad | Motivo |
|---|---:|---|
| `Overdraw` en `item_artist_album.xml` e `item_collector_performer.xml` | 2 | Falso positivo, igual que en el Sprint 2: `?attr/selectableItemBackground` solo pinta el efecto al tocar. Pasarlo a `android:foreground` exige API 23 en un `LinearLayout` y la app soporta desde API 21 |
| `MissingApplicationIcon` | 1 | No afecta el desempeño |
| `GradleDependency`, `AndroidGradlePluginVersion` | 45 | Las versiones de AndroidX están fijadas en `gradle/libs.versions.toml` para mantener `minSdk 21` (Android 5.0); varias versiones más recientes exigen `minSdk 23` |

## Verificación

| Verificación | Resultado |
|---|---|
| Pruebas unitarias (`./gradlew testDebugUnitTest`) | 175 de 175 pasan |
| Android Lint (`./gradlew lintDebug`) | 0 errores y ningún hallazgo nuevo |
| Medición en dispositivo | Ver el [[análisis de desempeño del Sprint 3|Sprint-3]] |

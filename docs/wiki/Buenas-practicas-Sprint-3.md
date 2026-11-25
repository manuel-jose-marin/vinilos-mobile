# Buenas prácticas de memoria, ANRs y accesibilidad · Sprint 3

Prácticas que usa la app para no retener memoria, no bloquear el hilo principal (ANR) y ser accesible, con el lugar del código donde se aplican. Cubre las ocho historias: las de HU01 a HU05 vienen de [[corrutinas y memoria del Sprint 2|Corrutinas-y-memoria-Sprint-2]] y siguen vigentes, y las pantallas nuevas del Sprint 3 aplican las mismas reglas. Las micro-optimizaciones están en [[micro-optimizaciones del Sprint 3|Micro-optimizaciones-Sprint-3]].

## Evitar ANRs

Un ANR ocurre cuando el hilo principal queda bloqueado unos 5 segundos y no puede responder al usuario. La app no hace red, disco ni conversión de JSON en ese hilo.

| Práctica | Dónde se aplica | Historias |
|---|---|---|
| Las peticiones HTTP las hace Volley en sus propios hilos; los servicios las exponen como funciones `suspend`, así que el ViewModel espera sin bloquear | `NetworkServiceAdapter` (`getArray`, `getObject`, `post`) | HU01 a HU08 |
| El JSON se convierte en modelos en `Dispatchers.Default` y no en el hilo principal, donde Volley entrega la respuesta | `parseOffMainThread` en los 12 métodos de `AlbumService`, `ArtistService`, `CollectorService` y `PrizeService`, incluidos `getCollector`, `getCollectorAlbums`, `createAlbum` y `addTrack` | HU01 a HU08 |
| Las corrutinas se lanzan en `viewModelScope`: se cancelan al destruir el ViewModel y, con ellas, la petición de Volley | Todos los ViewModels | HU01 a HU08 |
| "Reintentar" cancela la carga en curso antes de empezar otra, para no tener dos peticiones a la vez ni una respuesta vieja que reemplace a la nueva | `loadJob` en `AlbumListViewModel`, `AlbumDetailViewModel`, `ArtistListViewModel`, `ArtistDetailViewModel`, `CollectorListViewModel` y `CollectorDetailViewModel` | HU01 a HU06 |
| Las peticiones independientes de una misma pantalla van en paralelo con `async`, así la pantalla espera la más lenta y no la suma de todas | `AlbumRepository.getAlbum`, `ArtistRepository`, `CollectorRepository.getCollectorDetail` | HU02, HU03, HU04, HU06 |
| La validación de los formularios es en memoria y no toca la red | `AlbumFormValidator`, `TrackFormValidator` | HU07, HU08 |
| Mientras se guarda, el formulario y la hoja se deshabilitan y no se pueden cerrar, así que el usuario no lanza otra acción sobre una petición en curso | `CreateAlbumFragment` (`blockBackWhileSaving`), `AddTrackBottomSheet` (`isCancelable = false` al guardar) | HU07, HU08 |
| StrictMode en las compilaciones de depuración registra en Logcat cualquier acceso a red o disco en el hilo principal | `MainActivity.enableStrictMode` | Todas |

## Consumo de memoria

| Práctica | Dónde se aplica | Historias |
|---|---|---|
| El binding se libera en `onDestroyView`, para que el fragmento no retenga la jerarquía de vistas cuando queda en la pila de navegación | Los 7 fragmentos y `AddTrackBottomSheet` | HU01 a HU08 |
| El adaptador de cada lista se suelta en `onDestroyView` (`adapter = null`), porque guarda referencias a las vistas de sus filas | `AlbumListFragment`, `ArtistListFragment`, `ArtistDetailFragment`, `CollectorListFragment`, `CollectorDetailFragment` | HU01, HU03, HU04, HU05, HU06 |
| Las listas usan `ListAdapter` con `DiffUtil`, que solo redibuja las filas que cambian, y reciclan las filas al desplazarse | Listado de álbumes, listado de artistas, carrusel de álbumes del artista, listado de coleccionistas y carrusel de favoritos | HU01, HU03, HU04, HU05, HU06 |
| Glide carga las imágenes con `with(view)`, ligado al ciclo de vida de la vista; las reduce al tamaño del `ImageView` y las guarda en caché de memoria y disco | Portadas, fotos de artistas, integrantes, favoritos y vista previa de la portada | HU01 a HU04, HU06, HU07 |
| La vista previa de la portada se libera con `Glide.clear()` al destruir la vista, y su espera corre en `viewLifecycleOwner.lifecycleScope`, que se cancela con la vista | `CreateAlbumFragment` | HU07 |
| Las respuestas de los formularios vuelven a la pantalla anterior con la Fragment Result API, sin ViewModels compartidos que vivan más que la pantalla que los usa | `CreateAlbumFragment` → catálogo; `AddTrackBottomSheet` → detalle del álbum | HU07, HU08 |
| Un track agregado se suma a la lista que ya está en memoria, sin volver a descargar el álbum completo | `AlbumDetailViewModel.onTrackAdded` | HU08 |
| Las listas cortas dentro de un detalle (tracks, comentarios, premios, integrantes y la colección del coleccionista) se inflan en un `LinearLayout` en vez de anidar un `RecyclerView` en el `ScrollView` | Detalles de álbum, artista y coleccionista | HU02, HU04, HU06 |
| Los callbacks que llegan después de una espera revisan que la vista siga viva (`_binding?.`) antes de tocarla | `CreateAlbumFragment` (vista previa y desplazamiento al primer error), `ArtistDetailFragment` ("Leer más") | HU04, HU07 |

## Accesibilidad

El resultado completo de la revisión, con Accessibility Test Framework (el motor de Accessibility Scanner), está en la [[revisión de accesibilidad del Sprint 3|Accesibilidad-Sprint-3]]. Estas son las prácticas que quedaron en el código:

| Práctica | Dónde se aplica | Historias |
|---|---|---|
| Toda imagen con información tiene `contentDescription` (portadas, fotos de artistas); las decorativas usan `importantForAccessibility="no"` | Tarjetas y detalles | HU01 a HU06 |
| Los botones de solo ícono dicen la acción: "Llamar a Manolo Bellon al…", "Escribir un correo a…", "Ver los géneros", "Ver los sellos discográficos" | Detalle de coleccionista y formulario de álbum | HU06, HU07 |
| Áreas táctiles de al menos 48 dp en botones, chips, filas y en los botones de la hoja "Agregar track" | Todos los layouts | HU01 a HU08 |
| Cada campo usa el hint de `TextInputLayout` como etiqueta, y los errores se muestran con `setError`, que TalkBack anuncia | Formulario de álbum y hoja "Agregar track" | HU07, HU08 |
| Al fallar la validación, el foco pasa al primer campo con error, y el formulario de álbum se desplaza hasta él | `CreateAlbumFragment`, `AddTrackBottomSheet` (`focusFirstError`) | HU07, HU08 |
| Los títulos de sección están marcados como encabezados con `ViewCompat.setAccessibilityHeading`, que funciona desde Android 5.0, para saltar entre secciones con TalkBack | `markAsHeadings` en los 9 títulos de los detalles de álbum, artista y coleccionista | HU02, HU04, HU06 |
| Textos en `sp` y contraste de al menos 4.5:1 en el texto y 3:1 en los gráficos (estrellas y foto genérica de artista) | Tema `Theme.Vinilos`, `amber_dark`, `ic_artist_placeholder` | HU01 a HU08 |
| Los nombres pueden ocupar dos líneas y las filas crecen con el texto, para que con letra grande no se corten ni se encimen | Tarjetas de álbum, artista, coleccionista y colección | HU01, HU03, HU05, HU06 |
| Ningún dato depende solo del color: el estado de un álbum de la colección lleva punto y texto ("Disponible") | Detalle de coleccionista | HU06 |
| Las confirmaciones y los errores usan `Snackbar`, que TalkBack lee | Catálogo, detalle del álbum y formularios | HU07, HU08 |
| Con el teclado abierto, la hoja "Agregar track" se redimensiona (`adjustResize`) y su contenido se desplaza, para que "Cancelar" y "Agregar" sigan visibles | `AddTrackBottomSheet` | HU08 |
| Las pruebas de accesibilidad corren con el resto de la suite de Espresso y fallan si alguna pantalla tiene errores | `AccessibilityTest`, `AccessibilityFixesTest` | HU01 a HU08 |

# Buenas prácticas para evitar ANR y reducir el consumo de memoria · Sprint 2

Prácticas aplicadas en las cinco historias de usuario de las iteraciones 1 y 2 (HU01 a HU05). La primera parte explica cómo la app evita los errores "Application Not Responding" (ANR); la segunda, cómo reduce su consumo de memoria. Al final está la tabla por historia.

## Evitar ANR: el hilo principal nunca espera

Android muestra un ANR cuando el hilo principal (el de la interfaz) queda bloqueado unos 5 segundos. En Vinilos el hilo principal solo dibuja y responde al usuario; todo lo que puede tardar corre en otros hilos.

| Práctica | Cómo se aplica | Dónde |
|---|---|---|
| Peticiones HTTP fuera del hilo principal | Volley ejecuta cada petición y convierte el texto de la respuesta a `JSONArray` / `JSONObject` en sus propios hilos de red | `NetworkServiceAdapter` |
| Funciones `suspend` en lugar de callbacks | `NetworkServiceAdapter` envuelve cada petición de Volley con `suspendCancellableCoroutine`; los servicios, repositorios y ViewModels esperan la respuesta sin bloquear ningún hilo | `NetworkServiceAdapter`, todos los servicios y repositorios |
| **Conversión de JSON a modelos fuera del hilo principal** (Sprint 2) | Cada servicio convierte la respuesta en modelos con `withContext(Dispatchers.Default)`. Antes esa conversión corría en el hilo principal al volver de Volley; con respuestas grandes (álbumes con tracks y comentarios, artistas con álbumes y premios) podía demorar el dibujo de la pantalla | `NetworkAlbumService`, `NetworkArtistService`, `NetworkCollectorService`, `NetworkPrizeService` |
| Corrutinas atadas al ciclo de vida | Los cinco ViewModels lanzan sus cargas en `viewModelScope`; si el usuario sale de la pantalla, la corrutina se cancela | `AlbumListViewModel`, `AlbumDetailViewModel`, `ArtistListViewModel`, `ArtistDetailViewModel`, `CollectorListViewModel` |
| Cancelar la petición al cancelar la corrutina | `invokeOnCancellation { request.cancel() }`: una petición que ya nadie espera no sigue ocupando la red ni entrega su respuesta | `NetworkServiceAdapter` |
| Peticiones independientes en paralelo | Con `async`, las peticiones que necesita una misma pantalla salen al mismo tiempo: el tiempo de carga es el de la más lenta y no la suma de todas | `AlbumRepository.getAlbum` (álbum y coleccionistas), `ArtistRepository.getArtists` (músicos y bandas), `ArtistRepository.getArtist` (artista y premios) |
| Tiempo de espera acotado | Cada petición espera hasta 30 s y reintenta una vez; después la pantalla muestra el error con "Reintentar" en lugar de quedarse cargando | `NetworkServiceAdapter` |
| Filtros sin red | El filtro Todos / Músicos / Bandas se aplica en memoria sobre la lista ya cargada | `ArtistListViewModel.setFilter` |

## Reducir el consumo de memoria

| Práctica | Cómo se aplica | Dónde |
|---|---|---|
| Sin fugas de la Activity | El singleton `NetworkServiceAdapter` guarda solo `applicationContext`, nunca la Activity ni un Fragment. Los ViewModels no guardan vistas ni contextos | `NetworkServiceAdapter`, ViewModels |
| Liberar las vistas al salir de una pantalla | Cada fragmento pone `_binding = null` en `onDestroyView` y quita el adaptador de su `RecyclerView`, para que el fragmento que queda en la pila de navegación no retenga su jerarquía de vistas | Los cinco fragmentos |
| Tareas diferidas que no tocan vistas destruidas | El cálculo de "Leer más" en el detalle de artista se hace con `post { }` y primero revisa que la vista siga viva | `ArtistDetailFragment` |
| Imágenes con Glide | Glide reduce cada imagen al tamaño de su `ImageView` antes de guardarla en memoria, mantiene cachés en memoria y disco, reutiliza bitmaps y libera memoria cuando el sistema la pide | Portadas, fotos de artistas e integrantes |
| Glide atado al ciclo de vida | `Glide.with(view)` cancela las descargas pendientes cuando la pantalla se destruye | Adaptadores y fragmentos |
| **Liberar las imágenes de las filas recicladas** (Sprint 2) | `onViewRecycled` llama a `clearGlideImage(view)`: el bitmap de una fila que sale de la pantalla vuelve al pool de Glide en lugar de quedar retenido por la fila reciclada. Usa el contexto de la aplicación, porque al salir de la app las filas se reciclan con la Activity ya destruida y `Glide.with(view)` fallaba en ese momento (defecto #138, encontrado en el perfilamiento) | `AlbumAdapter`, `ArtistAdapter`, adaptador del carrusel de álbumes del detalle de artista |
| Listas eficientes | `ListAdapter` con `DiffUtil` solo vuelve a dibujar las filas que cambian; `RecyclerView` reutiliza las filas en lugar de crear una por elemento | Catálogo de álbumes, listado de artistas, listado de coleccionistas, carrusel de álbumes |
| **Listas que no se vuelven a medir** (Sprint 2) | `setHasFixedSize(true)` en las tres listas principales, que ocupan toda la pantalla: cambiar su contenido no obliga a medir de nuevo la lista completa | `AlbumListFragment`, `ArtistListFragment`, `CollectorListFragment` |
| Datos que sobreviven a la navegación | Los ViewModels conservan lo cargado al rotar la pantalla y al volver del detalle al listado; no se vuelve a pedir ni a guardar dos veces la misma lista | Los cinco ViewModels |
| Sin fondos repetidos | Las raíces de los layouts no pintan fondo encima del de la ventana (ver [[Micro-optimizaciones|Micro-optimizaciones-Sprint-2]]) | Layouts de las cinco historias |

## Por historia de usuario

| Historia | Evitar ANR | Memoria |
|---|---|---|
| HU01 Catálogo de álbumes | `GET /albums` en Volley, conversión en `Dispatchers.Default`, carga en `viewModelScope` | Glide con portadas al tamaño de la tarjeta y liberadas al reciclar, `ListAdapter`, lista de tamaño fijo, binding y adaptador liberados |
| HU02 Detalle de álbum | Álbum y coleccionistas en paralelo, conversión en `Dispatchers.Default` | Portada con Glide, binding liberado, el detalle se conserva al rotar |
| HU03 Listado de artistas | Músicos y bandas en paralelo, conversión en `Dispatchers.Default`, filtro en memoria | Fotos con Glide liberadas al reciclar, `ListAdapter`, lista de tamaño fijo, binding y adaptador liberados, el filtro se conserva al volver |
| HU04 Detalle de artista | Artista y premios en paralelo, conversión en `Dispatchers.Default` | Carrusel con `ListAdapter` y portadas liberadas al reciclar, `post { }` protegido, binding y adaptador liberados |
| HU05 Listado de coleccionistas | `GET /collectors` en Volley, conversión en `Dispatchers.Default`, carga en `viewModelScope` | `ListAdapter`, lista de tamaño fijo, binding y adaptador liberados |

## Verificación

- `./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` termina sin errores; las 80 pruebas unitarias pasan.
- El consumo real de CPU, memoria y red en dispositivos físicos está en el análisis de desempeño del Sprint 2.

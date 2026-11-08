# Inventario de pruebas · Sprint 2

Casos de prueba de **HU03 Consultar el listado de artistas**, **HU04 Consultar la información detallada de un artista** y **HU05 Consultar listado de coleccionistas**. Cada caso indica el criterio de aceptación que cubre y si se ejecuta como prueba E2E automatizada con Espresso o manual. Cómo se planean y ejecutan está detallado en la [Estrategia de pruebas](Estrategia-pruebas-Sprint-2).

**Entorno:** emulador o dispositivo físico con Android 5.0 (API 21) o superior, animaciones del sistema desactivadas para Espresso, y BackVynils corriendo localmente o en Render (`https://backvynils-grupo5.onrender.com/`).

## Resumen

| HU | Casos | Espresso | Manuales |
|---|---|---|---|
| HU03 Consultar listado de artistas | 6 | 3 | 3 |
| HU04 Consultar detalle de artista | 7 | 4 | 3 |
| HU05 Consultar listado de coleccionistas | 4 | 1 | 3 |
| **Total** | **17** | **8** | **9** |

## HU03 · Consultar el listado de artistas

| ID | Criterio | Caso | Precondición | Pasos | Resultado esperado | Tipo |
|---|---|---|---|---|---|---|
| HU03-01 | 1 | Ver el listado unificado de artistas | El backend tiene músicos y bandas | 1. Abrir la app en la sección Artistas | Se ve la lista/cuadrícula con avatar, nombre, tipo ("Músico" o "Banda") y número de álbumes de cada artista | Espresso |
| HU03-02 | 2 | Filtrar artistas por categoría | Pantalla de Artistas cargada | 1. Tocar el filtro "Músicos"<br>2. Tocar el filtro "Bandas"<br>3. Tocar "Todos" | Se filtra la lista en memoria mostrando solo músicos, solo bandas o todos respectivamente, sin hacer nuevas peticiones de red | Espresso |
| HU03-03 | 3 | Ver tarjetas de carga | Conexión lenta (perfil de red en emulador) | 1. Abrir la sección Artistas | Mientras llega la respuesta se ven componentes de carga; al llegar los datos se reemplazan por los artistas | Manual |
| HU03-04 | 4 | Error sin conexión y reintento | Modo avión activado | 1. Abrir la sección Artistas<br>2. Desactivar modo avión<br>3. Tocar "Reintentar" | Paso 1: se ve el mensaje de error y el botón "Reintentar". Paso 3: se carga el listado de artistas | Manual |
| HU03-05 | 5 | Listado de artistas vacío | El backend no tiene músicos ni bandas | 1. Abrir la sección Artistas | Se ve el mensaje indicando que no hay artistas | Manual |
| HU03-06 | 6 | Abrir el detalle desde el listado | El backend tiene artistas | 1. Abrir la sección Artistas<br>2. Tocar una tarjeta de artista | Se abre la pantalla de detalle de ese artista | Espresso |

## HU04 · Consultar la información detallada de un artista

| ID | Criterio | Caso | Precondición | Pasos | Resultado esperado | Tipo |
|---|---|---|---|---|---|---|
| HU04-01 | 1 | Ver la ficha técnica del artista | El backend tiene artistas | 1. En el listado, tocar un artista | Se ve imagen, nombre, badge (Músico o Banda), fecha (nacimiento o creación) y biografía/descripción | Espresso |
| HU04-02 | 2 | Ver carrusel de álbumes | Artista con álbumes asociados | 1. Abrir el detalle del artista | Se ve el carrusel de álbumes del artista con sus portadas y títulos | Espresso |
| HU04-03 | 3 | Ver lista de premios | Artista con premios obtenidos | 1. Abrir el detalle del artista | Se ve la sección de premios mostrando fecha de premiación, nombre y organización del premio | Espresso |
| HU04-04 | 4 | Ver integrantes de una banda | Seleccionar una banda (p. ej. "Queen") | 1. Abrir el detalle de la banda | Se ve la sección de integrantes con la lista de músicos que la conforman | Espresso |
| HU04-05 | 5 | Secciones vacías | Artista sin premios o sin álbumes | 1. Abrir el detalle de dicho artista | Las secciones correspondientes muestran el mensaje de lista vacía | Manual |
| HU04-06 | 6 | Error sin conexión y reintento | Listado cargado; luego modo avión activado | 1. Tocar un artista<br>2. Desactivar modo avión<br>3. Tocar "Reintentar" | Paso 1: se ve mensaje de error con "Reintentar". Paso 3: se carga el detalle del artista | Manual |
| HU04-07 | 7 | Volver al listado de artistas | Detalle de un artista abierto | 1. Tocar la flecha de regreso o botón atrás del sistema | Se regresa al listado de artistas | Espresso |

## HU05 · Consultar listado de coleccionistas

| ID | Criterio | Caso | Precondición | Pasos | Resultado esperado | Tipo |
|---|---|---|---|---|---|---|
| HU05-01 | 1 | Ver el listado de coleccionistas | El backend tiene coleccionistas | 1. Abrir la app en la sección Coleccionistas | Se ve la lista con iniciales/avatar, nombre, correo electrónico y número de álbumes coleccionados | Espresso |
| HU05-02 | 2 | Ver tarjetas de carga | Conexión lenta | 1. Abrir la sección Coleccionistas | Se muestran indicadores de carga antes de desplegar los coleccionistas | Manual |
| HU05-03 | 3 | Error sin conexión y reintento | Modo avión activado | 1. Abrir la sección Coleccionistas<br>2. Desactivar modo avión<br>3. Tocar "Reintentar" | Paso 1: mensaje de error con botón "Reintentar". Paso 3: carga exitosa de la lista | Manual |
| HU05-04 | 4 | Lista de coleccionistas vacía | El backend no tiene coleccionistas | 1. Abrir la sección Coleccionistas | Se ve el mensaje indicando que no hay coleccionistas registrados | Manual |

## Pruebas que respaldan cada caso

| Caso | Prueba Espresso | Pruebas Unitarias |
|---|---|---|
| HU03-01 | `ArtistListTest.listShowsMusiciansAndBands` | `ArtistServiceTest.parseMusicians`, `ArtistServiceTest.parseBands` |
| HU03-02 | `ArtistListTest.filteringByMusiciansOrBands` | `ArtistListViewModelTest.setFilter_filtersInMemory` |
| HU03-03 | | `ArtistListViewModelTest.init_showsLoading` |
| HU03-04 | | `ArtistListViewModelTest.loadArtists_error`, `ArtistListViewModelTest.retry_loadsAgain` |
| HU03-05 | | `ArtistListViewModelTest.loadArtists_empty` |
| HU03-06 | `ArtistListTest.tappingArtistOpensDetail` | |
| HU04-01 | `ArtistDetailTest.detailShowsArtistDataSheet` | `ArtistServiceTest.parseMusicianDetail`, `ArtistServiceTest.parseBandDetail` |
| HU04-02 | `ArtistDetailTest.detailShowsAlbumsCarousel` | `ArtistDetailViewModelTest.loadArtist_includesAlbums` |
| HU04-03 | `ArtistDetailTest.detailShowsPrizes` | `PrizeServiceTest.parsePrizes`, `ArtistRepositoryTest.joinsPrizes` |
| HU04-04 | `ArtistDetailTest.bandDetailShowsMembers` | `ArtistServiceTest.parseBandMembers` |
| HU04-05 | | `ArtistDetailViewModelTest.emptySections` |
| HU04-06 | | `ArtistDetailViewModelTest.loadArtist_error` |
| HU04-07 | `ArtistDetailTest.backNavigationReturnsToList` | |
| HU05-01 | `CollectorListTest.listShowsCollectors` | `CollectorServiceTest.parseCollectors` |
| HU05-02 | | `CollectorListViewModelTest.init_showsLoading` |
| HU05-03 | | `CollectorListViewModelTest.loadCollectors_error` |
| HU05-04 | | `CollectorListViewModelTest.loadCollectors_empty` |

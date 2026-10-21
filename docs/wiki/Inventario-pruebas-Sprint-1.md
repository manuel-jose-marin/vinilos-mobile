# Inventario de pruebas · Sprint 1

Casos de prueba de **HU01 Consultar catálogo de álbumes** y **HU02 Consultar la información detallada de un álbum**, derivados de los criterios de aceptación de [#1](https://github.com/manuel-jose-marin/vinilos-mobile/issues/1) y [#2](https://github.com/manuel-jose-marin/vinilos-mobile/issues/2). Cada caso indica el criterio que cubre y si se ejecuta como prueba E2E manual o automatizada con Espresso.

**Entorno:** emulador o dispositivo con Android 5.0 (API 21) o superior, animaciones del sistema desactivadas para Espresso, y BackVynils corriendo con datos de prueba (al menos un álbum con tracks y comentarios, y otro sin tracks ni comentarios).

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
| HU01-04 | 4 | Catálogo vacío | El backend no tiene álbumes | 1. Abrir la sección Álbumes | Se ve el mensaje de que no hay álbumes | Manual |
| HU01-05 | 5 | Portada que no carga | Un álbum con URL de portada inválida | 1. Abrir la sección Álbumes | La tarjeta de ese álbum muestra la imagen genérica | Manual |
| HU01-06 | 6 | Abrir el detalle desde el catálogo | El backend tiene álbumes | 1. Abrir la sección Álbumes<br>2. Tocar la tarjeta de un álbum | Se abre el detalle de ese álbum | Espresso |
| HU01-07 | 7 | Abrir el formulario Crear álbum | Requiere HU07 | 1. Tocar "Nuevo álbum" | Se abre el formulario Crear álbum | Fuera del Sprint 1 (se valida con HU07 en el Sprint 3) |
| HU01-08 | 8 | Último álbum visible sobre el botón | El backend tiene más álbumes de los que caben en pantalla | 1. Abrir la sección Álbumes<br>2. Desplazarse hasta el final | El último álbum queda visible por encima del botón "Nuevo álbum" | Espresso |
| HU01-09 | 9 | Álbum nuevo en el catálogo | Requiere HU07 | 1. Crear un álbum<br>2. Volver al catálogo | El álbum nuevo aparece en el catálogo | Fuera del Sprint 1 (se valida con HU07 en el Sprint 3) |

## HU02 · Consultar la información detallada de un álbum

| ID | Criterio | Caso | Precondición | Pasos | Resultado esperado | Tipo |
|---|---|---|---|---|---|---|
| HU02-01 | 1 | Ver la ficha del álbum | El backend tiene álbumes | 1. En el catálogo, tocar un álbum | Se ven portada, nombre, artista, fecha de lanzamiento, género, sello y descripción del álbum tocado | Espresso |
| HU02-02 | 2 | Ver los tracks | Álbum con tracks, uno de ellos con nombre largo | 1. Abrir el detalle del álbum | Se ve "Tracks · N" y cada track muestra número, nombre y duración; el nombre largo aparece completo en dos líneas | Espresso |
| HU02-03 | 3 | Ver los comentarios | Álbum con comentarios | 1. Abrir el detalle del álbum | Cada comentario muestra autor, calificación de 1 a 5 y texto; no hay opción para escribir comentarios | Espresso |
| HU02-04 | 4 | Secciones vacías | Álbum sin tracks ni comentarios | 1. Abrir el detalle del álbum | Las secciones de tracks y comentarios muestran el mensaje de lista vacía | Manual |
| HU02-05 | 5 | Error sin conexión y reintento | Catálogo cargado; luego modo avión activado | 1. Tocar un álbum<br>2. Desactivar el modo avión<br>3. Tocar "Reintentar" | Paso 1: se ve el mensaje de error con "Reintentar". Paso 3: se carga el detalle | Manual |
| HU02-06 | 6 | Abrir el formulario Agregar track | Requiere HU08 | 1. En el detalle, tocar "Agregar track" | Se abre el formulario para agregar un track | Fuera del Sprint 1 (se valida con HU08 en el Sprint 3) |
| HU02-07 | 7 | Volver al catálogo | Detalle de un álbum abierto | 1. Tocar la flecha de regreso<br>2. Abrir otro detalle y tocar el botón atrás del sistema | En ambos casos se vuelve al catálogo | Espresso |

## Automatización

| Clase Espresso | Casos | Issue |
|---|---|---|
| `AlbumListTest` | HU01-01, HU01-06, HU01-08 | [#17](https://github.com/manuel-jose-marin/vinilos-mobile/issues/17) |
| `AlbumDetailTest` | HU02-01, HU02-02, HU02-03, HU02-07 | [#22](https://github.com/manuel-jose-marin/vinilos-mobile/issues/22) |

Los casos de carga, error de red, lista vacía y portada inválida quedan como manuales porque dependen de condiciones de red o de datos del backend que las pruebas E2E contra BackVynils no controlan.

## Resultados

_Pendiente: se registran al ejecutar las pruebas al cierre del sprint. Los defectos encontrados se reportan como issues con la etiqueta `bug`._

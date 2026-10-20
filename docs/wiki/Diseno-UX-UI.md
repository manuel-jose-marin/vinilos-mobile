# Diseño UX/UI

Prototipo navegable compuesto de mockups (un nivel de detalle mayor a los wireframes) con las pantallas, componentes y transiciones de las 8 historias de usuario del MVP, construido en Google Stitch inicialmente y posteriormente refinado y mejorado en la herramienta de diseño open source Penpot.

### ▶️ [Abrir prototipo navegable Stitch](https://stitch.withgoogle.com/preview/8794366402634156219?node-id=826fc561b2514af3b1626060245f6f66)

### ▶️ [Abrir prototipo navegable Penpot](https://design.penpot.app/#/view?file-id=17193a1f-939a-80b5-8008-c515c098fe5d&page-id=17193a1f-939a-80b5-8008-c515c098fe5e&section=interactions&index=0&share-id=cd240047-b7fb-4186-98f1-b9598e3409a8)

---

## Sistema de diseño

Basado en **Material Design 3**, el estándar nativo de Android.

| Elemento | Definición |
|---|---|
| Color primario | Vino tinto `#7A1F35` |
| Color secundario | Ámbar `#C8892B` |
| Superficies | Tonos crema claros, tema claro |
| Tipografía | Roboto (escalas Headline y Body) |
| Formas | Tarjetas con esquinas de 16dp, botones tipo píldora |
| Navegación | Barra inferior con 3 destinos: Álbumes, Artistas, Coleccionistas |
| Accesibilidad | Áreas táctiles mínimas de 48dp, contraste de texto mínimo 4.5:1, íconos con etiqueta |

## Pantallas por historia de usuario

<table>
<tr>
<td align="center"><b>HU01</b><br>Listado de álbumes</td>
<td align="center"><b>HU02</b><br>Detalle de álbum</td>
<td align="center"><b>HU03</b><br>Listado de artistas</td>
<td align="center"><b>HU04</b><br>Detalle de artista</td>
</tr>
<tr>
<td><img width="200" alt="Listado de álbumes" src="https://github.com/user-attachments/assets/cf46c728-12e1-444b-a002-835cfde1cb2b" /></td>
<td><img width="200" alt="Detalle de álbum" src="https://github.com/user-attachments/assets/f8a2ebd8-4490-4cd0-a1cb-47d9a8bee0b8" /></td>
<td><img width="200" alt="Listado de artistas" src="https://github.com/user-attachments/assets/ae0aaab5-2b94-4e94-8cf1-4a027f549836" /></td>
<td><img width="200" alt="Detalle de artista" src="https://github.com/user-attachments/assets/c9033a08-227b-4d48-8889-1dfec2eeaf22" /></td>
</tr>
<tr>
<td align="center"><b>HU05</b><br>Listado de coleccionistas</td>
<td align="center"><b>HU06</b><br>Detalle de coleccionista</td>
<td align="center"><b>HU07</b><br>Crear álbum</td>
<td align="center"><b>HU08</b><br>Agregar track</td>
</tr>
<tr>
<td><img width="200" alt="Listado de coleccionistas" src="https://github.com/user-attachments/assets/25b54947-4ab7-4529-9de5-e2aedda14aa0" /></td>
<td><img width="200" alt="Detalle de coleccionista" src="https://github.com/user-attachments/assets/d154d5a5-d731-477b-b84a-d4f9fd86e078" /></td>
<td><img width="200" alt="Crear álbum" src="https://github.com/user-attachments/assets/2947ee95-ce68-4cbc-898f-88969e89789f" /></td>
<td><img width="200" alt="Agregar track" src="https://github.com/user-attachments/assets/8d649b34-3e41-4f2d-b251-a3685083f9e4" /></td>
</tr>
</table>

## Componentes por pantalla

| HU | Pantalla | Componentes principales |
|---|---|---|
| HU01 | Listado de álbumes | Barra superior con búsqueda, chips de género, cuadrícula de tarjetas (portada, nombre, artista, año y género), botón flotante "Nuevo álbum" |
| HU02 | Detalle de álbum | Portada, chips de fecha, género y sello, descripción, lista de tracks con duración, comentarios de solo lectura, botón "Agregar track" |
| HU03 | Listado de artistas | Botón segmentado Todos / Músicos / Bandas, lista con avatar, nombre, tipo y número de álbumes |
| HU04 | Detalle de artista | Imagen, chip Músico o Banda, fecha, descripción, carrusel de álbumes, premios e integrantes (bandas) |
| HU05 | Listado de coleccionistas | Lista con iniciales, nombre, correo y número de álbumes |
| HU06 | Detalle de coleccionista | Datos de contacto, artistas favoritos, colección con precio y estado, comentarios |
| HU07 | Crear álbum | Formulario con nombre, URL y vista previa de portada, fecha, descripción, género y sello; validación de campos obligatorios |
| HU08 | Agregar track | Hoja inferior modal con nombre del track y duración (mm:ss) |

## Flujo de navegación

```mermaid
flowchart LR
    NAV{{Barra inferior}} --> A[Listado de álbumes]
    NAV --> R[Listado de artistas]
    NAV --> C[Listado de coleccionistas]
    A -->|Tocar álbum| AD[Detalle de álbum]
    A -->|Nuevo álbum| CA[Crear álbum]
    CA -->|Crear álbum| A
    AD -->|Agregar track| AT[Agregar track]
    AT -->|Agregar| AD
    R -->|Tocar artista| RD[Detalle de artista]
    RD -->|Tocar álbum| AD
    C -->|Tocar coleccionista| CD[Detalle de coleccionista]
```

| Desde | Acción | Hacia |
|---|---|---|
| Cualquier pantalla principal | Barra inferior | Álbumes, Artistas o Coleccionistas |
| Listado de álbumes | Tocar una tarjeta | Detalle de álbum |
| Listado de álbumes | Botón "Nuevo álbum" | Crear álbum |
| Crear álbum | "Crear álbum" | Listado de álbumes con mensaje de confirmación |
| Detalle de álbum | "Agregar track" | Hoja inferior Agregar track |
| Agregar track | "Agregar" | Detalle de álbum con el track agregado |
| Listado de artistas | Tocar un artista | Detalle de artista |
| Detalle de artista | Tocar un álbum del carrusel | Detalle de álbum |
| Listado de coleccionistas | Tocar un coleccionista | Detalle de coleccionista |
| Pantallas de detalle y formularios | Flecha de regreso | Pantalla anterior |
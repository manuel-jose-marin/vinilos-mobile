# Sprint 1 (2026-10-19 a 2026-11-01)

**Historias:** HU01 Consultar catálogo de álbumes, HU02 Consultar la información detallada de un álbum

## Sprint Planning
- [[Actas de reunión|Actas_Sprint-1]]: evidencia de las reuniones de las semanas 3 y 4

## Diseño arquitectónico
- [[Diseño arquitectónico del Sprint 1|Diseno-arquitectonico-Sprint-1]]: capas MVVM, Service Adapter, navegación y pruebas

## Estrategia de pruebas
- [[Estrategia de pruebas del Sprint 1|Estrategia-pruebas-Sprint-1]]: aplicación bajo pruebas, objetivos, presupuesto, técnicas, niveles y tipos de prueba, relación entre criterios y pruebas, y cronograma

## Artefactos de prueba
- [[Inventario de pruebas|Inventario-pruebas-Sprint-1]]: consolidado de los 16 casos de prueba (13 aplicables ejecutados con 100% de éxito y 3 fuera del alcance), verificados en Android 15 (API 35) y Android 5 (API 21).
- **Scripts y casos de prueba automatizados en Espresso:**
  - [`AlbumListTest.kt`](https://github.com/manuel-jose-marin/vinilos-mobile/blob/develop/app/src/androidTest/java/co/edu/uniandes/vinilos/AlbumListTest.kt): automatiza los casos E2E del catálogo (`HU01-01` cuadrícula y contenido, `HU01-06` apertura del detalle y `HU01-08` visibilidad del último álbum).
  - [`AlbumDetailTest.kt`](https://github.com/manuel-jose-marin/vinilos-mobile/blob/develop/app/src/androidTest/java/co/edu/uniandes/vinilos/AlbumDetailTest.kt): automatiza los casos E2E del detalle (`HU02-01` ficha técnica del álbum, `HU02-02` lista de tracks y duraciones, `HU02-03` comentarios y `HU02-07` navegación de regreso).
- **Reporte de defectos:**
  - [Defecto #94 — HU02-02 trunca el nombre largo del track](https://github.com/manuel-jose-marin/vinilos-mobile/issues/94): Severidad media. Al consultar el detalle de un álbum, los nombres largos de los tracks se truncaban. Corregido y validado mediante el [PR #96](https://github.com/manuel-jose-marin/vinilos-mobile/pull/96).
  - [Defecto #105 — HU01-02 las tarjetas de carga del listado quedan pegadas](https://github.com/manuel-jose-marin/vinilos-mobile/issues/105): Severidad baja. Mientras carga el catálogo, las tarjetas de carga se veían pegadas y la pantalla muy llena, porque los `<include>` reemplazaban el margen de la tarjeta. El margen ahora va en cada `<include>`, así que tienen la misma separación que la cuadrícula de álbumes.

## APK y release
_Pendiente._

## Retrospectiva
- [[Retrospectiva del Sprint 1|Retrospectiva-Sprint-1]]: estrella de mar en Miro, seguimiento de las acciones del Inception y acciones de mejora para el Sprint 2

# Backlog de producto

MVP de 8 historias de usuario (HU01 a HU08). Estimación en puntos de historia con escala Fibonacci (1, 2, 3, 5, 8), por consenso del equipo considerando complejidad, incertidumbre y esfuerzo.

## Historias de usuario

| Id | Historia | Puntos | Sprint |
|---|---|---|---|
| HU01 | Consultar catálogo de álbumes | 5 | Sprint 1 |
| HU02 | Consultar la información detallada de un álbum | 3 | Sprint 1 |
| HU03 | Consultar el listado de artistas | 3 | Sprint 2 |
| HU04 | Consultar la información detallada de un artista | 5 | Sprint 2 |
| HU05 | Consultar listado de coleccionistas | 2 | Sprint 2 |
| HU06 | Consultar la información detallada de coleccionista | 3 | Sprint 3 |
| HU07 | Crear un álbum | 5 | Sprint 3 |
| HU08 | Asociar tracks con un álbum | 5 | Sprint 3 |

**Total:** 31 puntos. Sprint 1: 8 · Sprint 2: 10 · Sprint 3: 13. Al cierre del Sprint 2 hay 5 historias acumuladas y al cierre del Sprint 3, 8.

En el Sprint 1 se suman las tareas de configuración base (proyecto, red, navegación y backend), por eso lleva menos historias.

## Tareas por sprint

Cada tarea es un issue en GitHub con su milestone, etiqueta y responsable. Las tareas de cada historia están enlazadas como sub-issues del issue de la historia.


### Sprint 1 (2026-10-19 a 2026-11-01)

| Tarea | Tipo | Responsable |
|---|---|---|
| Crear proyecto Android base en Kotlin con estructura de paquetes MVVM | setup | Manuel José Marín |
| Configurar capa de red (Service Adapter) con la URL base del backend | setup | Miguel Rincón |
| Configurar navegación principal (Navigation Component y menú inferior) | setup | Felipe Rojas |
| Levantar el backend BackVynils y documentar cómo usarlo en el equipo | setup | Alejandro Pulido |
| [HU01] Diseñar layout del listado de álbumes (pantalla e ítem de lista) | diseño | Manuel José Marín |
| [HU01] Implementar modelo y servicio REST de álbumes (GET /albums) | implementación | Miguel Rincón |
| [HU01] Implementar repositorio y ViewModel del listado de álbumes | implementación | Felipe Rojas |
| [HU01] Implementar vista del listado de álbumes con RecyclerView y navegación al detalle | implementación | Alejandro Pulido |
| [HU01] Pruebas E2E con Espresso del listado de álbumes | pruebas | Manuel José Marín |
| [HU02] Diseñar layout del detalle de álbum | diseño | Miguel Rincón |
| [HU02] Implementar servicio REST del detalle de álbum (GET /albums/{id}) | implementación | Felipe Rojas |
| [HU02] Implementar repositorio y ViewModel del detalle de álbum | implementación | Alejandro Pulido |
| [HU02] Implementar vista del detalle de álbum y navegación desde el listado | implementación | Manuel José Marín |
| [HU02] Pruebas E2E con Espresso del detalle de álbum | pruebas | Miguel Rincón |
| Documentar diseño arquitectónico del Sprint 1 en la wiki | documentación | Felipe Rojas |
| Definir estrategia de pruebas del Sprint 1 | documentación | Alejandro Pulido |
| Inventario de pruebas y reporte de defectos del Sprint 1 | pruebas | Manuel José Marín |
| Generar APK, actualizar README y crear release del Sprint 1 | documentación | Miguel Rincón |
| Retrospectiva del Sprint 1 | documentación | Felipe Rojas |

### Sprint 2 (2026-11-02 a 2026-11-15)

| Tarea | Tipo | Responsable |
|---|---|---|
| [HU03] Diseñar layout del listado de artistas (músicos y bandas) (pantalla e ítem de lista) | diseño | Miguel Rincón |
| [HU03] Implementar modelo y servicio REST de artistas (músicos y bandas) (GET /musicians y GET /bands) | implementación | Felipe Rojas |
| [HU03] Implementar repositorio y ViewModel del listado de artistas (músicos y bandas) | implementación | Alejandro Pulido |
| [HU03] Implementar vista del listado de artistas (músicos y bandas) con RecyclerView y navegación al detalle | implementación | Manuel José Marín |
| [HU03] Pruebas E2E con Espresso del listado de artistas (músicos y bandas) | pruebas | Miguel Rincón |
| [HU04] Diseñar layout del detalle de artista (músico o banda) | diseño | Felipe Rojas |
| [HU04] Implementar servicio REST del detalle de artista (músico o banda) (GET /musicians/{id} y GET /bands/{id}) | implementación | Alejandro Pulido |
| [HU04] Implementar repositorio y ViewModel del detalle de artista (músico o banda) | implementación | Manuel José Marín |
| [HU04] Implementar vista del detalle de artista (músico o banda) y navegación desde el listado | implementación | Miguel Rincón |
| [HU04] Pruebas E2E con Espresso del detalle de artista (músico o banda) | pruebas | Felipe Rojas |
| [HU05] Diseñar layout del listado de coleccionistas (pantalla e ítem de lista) | diseño | Alejandro Pulido |
| [HU05] Implementar modelo y servicio REST de coleccionistas (GET /collectors) | implementación | Manuel José Marín |
| [HU05] Implementar repositorio y ViewModel del listado de coleccionistas | implementación | Miguel Rincón |
| [HU05] Implementar vista del listado de coleccionistas con RecyclerView y navegación al detalle | implementación | Felipe Rojas |
| [HU05] Pruebas E2E con Espresso del listado de coleccionistas | pruebas | Alejandro Pulido |
| Documentar diseño arquitectónico del Sprint 2 en la wiki | documentación | Manuel José Marín |
| Definir estrategia de pruebas del Sprint 2 | documentación | Miguel Rincón |
| Inventario de pruebas y reporte de defectos del Sprint 2 | pruebas | Felipe Rojas |
| Análisis de desempeño del Sprint 2 | pruebas | Alejandro Pulido |
| Generar APK, actualizar README y crear release del Sprint 2 | documentación | Manuel José Marín |
| Retrospectiva del Sprint 2 | documentación | Miguel Rincón |

### Sprint 3 (2026-11-16 a 2026-11-29)

| Tarea | Tipo | Responsable |
|---|---|---|
| [HU06] Diseñar layout del detalle de coleccionista | diseño | Felipe Rojas |
| [HU06] Implementar servicio REST del detalle de coleccionista (GET /collectors/{id}) | implementación | Alejandro Pulido |
| [HU06] Implementar repositorio y ViewModel del detalle de coleccionista | implementación | Manuel José Marín |
| [HU06] Implementar vista del detalle de coleccionista y navegación desde el listado | implementación | Miguel Rincón |
| [HU06] Pruebas E2E con Espresso del detalle de coleccionista | pruebas | Felipe Rojas |
| [HU07] Diseñar formulario de creación de álbum | diseño | Alejandro Pulido |
| [HU07] Implementar validación de campos del formulario de álbum | implementación | Manuel José Marín |
| [HU07] Implementar servicio REST de creación de álbum (POST /albums) | implementación | Miguel Rincón |
| [HU07] Implementar repositorio y ViewModel de creación de álbum con manejo de errores | implementación | Felipe Rojas |
| [HU07] Pruebas E2E con Espresso de creación de álbum | pruebas | Alejandro Pulido |
| [HU08] Diseñar formulario para agregar tracks desde el detalle del álbum | diseño | Manuel José Marín |
| [HU08] Implementar servicio REST para asociar tracks (POST /albums/{id}/tracks) | implementación | Miguel Rincón |
| [HU08] Implementar repositorio y ViewModel para agregar tracks | implementación | Felipe Rojas |
| [HU08] Mostrar tracks actualizados en el detalle del álbum | implementación | Alejandro Pulido |
| [HU08] Pruebas E2E con Espresso de asociación de tracks | pruebas | Manuel José Marín |
| Documentar diseño arquitectónico del Sprint 3 en la wiki | documentación | Miguel Rincón |
| Definir estrategia de pruebas del Sprint 3 | documentación | Felipe Rojas |
| Inventario de pruebas y reporte de defectos del Sprint 3 | pruebas | Alejandro Pulido |
| Análisis de desempeño del Sprint 3 | pruebas | Manuel José Marín |
| Pruebas de reconocimiento aleatorias y de exploración sistemática | pruebas | Miguel Rincón |
| Revisión de accesibilidad de la app | pruebas | Felipe Rojas |
| Generar APK, actualizar README y crear release del Sprint 3 | documentación | Alejandro Pulido |
| Retrospectiva del Sprint 3 | documentación | Manuel José Marín |

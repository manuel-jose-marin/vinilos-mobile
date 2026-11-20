# Retrospectiva del Sprint 2

Retrospectiva del equipo sobre el Sprint 2 (2026-11-02 a 2026-11-15, historias HU03, HU04 y HU05). Se contrastó lo planeado en el sprint planning del [[acta de la semana 5|Actas_Sprint-2]] con la evidencia del repositorio: issues, pull requests, inventario de pruebas, actas y la [[gráfica de velocidad del Sprint 2|Sprint-3]]. También se revisó el cumplimiento de las acciones que dejó la [[retrospectiva del Sprint 1|Retrospectiva-Sprint-1]].

## Comportamiento del equipo durante el Sprint 2

El equipo cumplió el objetivo del sprint con más alcance que en el anterior: las tres historias comprometidas (HU03, 3 SP; HU04, 5 SP, y HU05, 2 SP, para un total de 10 SP, 25 % más que la velocidad del Sprint 1) quedaron integradas en `develop` y publicadas en el release [v2.0.0](https://github.com/manuel-jose-marin/vinilos-mobile/releases/tag/v2.0.0). La app ya lista músicos y bandas con filtro, muestra el detalle de un artista con sus álbumes, premios e integrantes, y lista los coleccionistas. Las pruebas respaldan el resultado: el [[inventario de pruebas|Inventario-pruebas-Sprint-2]] tiene 22 casos con su evidencia, el sprint sumó 14 pruebas E2E con Espresso (21 en total con la regresión del Sprint 1) y 56 pruebas unitarias, y los dos defectos del sprint se resolvieron antes del cierre: el total de artistas que no se mostraba (#134), que encontró el inventario, y el crash al salir de la app (#138 a #142), que encontró el perfilamiento. Además de los puntos comprometidos, el equipo entregó el [[análisis de desempeño|Analisis-desempeno-Sprint-2]], las [[micro-optimizaciones|Micro-optimizaciones-Sprint-2]] y las prácticas de [[corrutinas y memoria|Corrutinas-y-memoria-Sprint-2]]. La acción que mejor funcionó fue cerrar cada issue con `Closes #N`: las 15 tareas con puntos se cerraron en el mismo minuto en que se fusionó su pull request, así que el tablero mostró el avance real.

Los problemas estuvieron otra vez en el ritmo y en la coordinación. La integración llegó por tandas (4, 8 y 13 de noviembre) y del 9 al 12 de noviembre no se integró ninguna tarea con puntos, porque las vistas esperaban sus repositorios y la aprobación de pull requests. El cierre se concentró en el último día: el 15 de noviembre se cerraron 14 de los 34 issues cerrados del sprint, entre ellos el inventario de pruebas (#45), el análisis de desempeño (#46) y el release (#47). Hubo retrabajo en #114, que resolvieron dos integrantes por separado (#136 y #144): #136 se fusionó y se revirtió una hora después con #147. La revisión de pull requests mejoró, pero sigue sin ser la regla: 8 de los 26 pull requests fusionados a `develop` tienen una aprobación registrada en GitHub, y la protección de la rama no la exige.

## Seguimiento de las acciones del Sprint 1

| Acción acordada en la retrospectiva del Sprint 1 | Estado | Evidencia |
|---|---|---|
| Todo pull request a `develop` necesita una aprobación registrada en GitHub y se activa la protección de la rama para exigirla. | Parcial | 8 de 26 pull requests con aprobación registrada (31 %), frente a 3 de 23 en el Sprint 1. `develop` está protegida, pero no exige aprobaciones. |
| Cada pull request lleva `Closes #N` y el issue se cierra al fusionarlo. | Cumplida | Las 15 tareas con puntos se cerraron en el mismo minuto en que se fusionó su pull request. |
| Dejar la documentación y las pruebas terminadas el jueves y anotar en el acta la fecha límite real. | No cumplida | El inventario (#45), el análisis de desempeño (#46) y el release (#47) se cerraron el domingo 15 de noviembre. |
| Confirmar con el curso las decisiones de infraestructura y probar contra la API desplegada antes de generar el APK. | Parcial | El APK v2.0.0 apunta al backend desplegado del equipo en Render (`https://backvynils-grupo5.onrender.com/`). La confirmación con el curso no quedó registrada en el repositorio. |
| Avisar qué archivos o documentos va a tocar cada quien antes de abrir un pull request que pueda pisar a otro. | No cumplida | #136 y #144 resolvieron la misma tarea (#114) y uno de los dos se revirtió (#147). |
| Escribir las pruebas E2E junto con la implementación y ejecutarlas en Android 15 o superior y en Android 5.0. | Parcial | Las E2E del listado de coleccionistas entraron el 8 de noviembre y las de artistas junto con sus vistas, el 13 y 14. El inventario se ejecutó en un emulador con Android 17 (API 37); no hay registro de una ejecución en Android 5.0. |
| Hacer el seguimiento asíncrono cada miércoles y viernes. | Sin evidencia en el repositorio | Se mantiene como acción. |
| Verificar las estimaciones y los datos de cada acta contra el backlog antes de publicarla. | Cumplida | El compromiso de 10 SP del acta de la semana 5 coincide con el [[backlog|Backlog]]. |
| Mantener la evidencia de pruebas por caso, el Definition of Done y la guía del backend. | Cumplida | El inventario registra evidencia por caso, el acta de la semana 5 tiene su Definition of Done y la [[guía de BackVynils|Backend-BackVynils]] sigue vigente. |

## Acciones de mejora para el Sprint 3

| Acción | Origen |
|---|---|
| Exigir una aprobación en la protección de `develop` y repartir las revisiones entre los cuatro integrantes. Quien no reciba revisión en 24 horas lo avisa en el canal del equipo. | Solo 8 de 26 pull requests con aprobación registrada |
| Integrar cada tarea apenas está lista, en pull requests pequeños, y empezar las tareas que dependen de otras contra servicios falsos (`fakes/`). | Cuatro días sin integrar tareas con puntos (9 a 12 de noviembre) |
| Revisar la gráfica de velocidad del Sprint 3 el miércoles 25 de noviembre y decidir ese día si algo se recorta. | El compromiso sube a 13 SP, 30 % más que la velocidad medida |
| Avisar en el canal del equipo antes de tomar una tarea que tiene otro responsable. | Retrabajo de #136 y #144 |
| Dejar la documentación y las pruebas no funcionales listas a más tardar el viernes 27 de noviembre, según el cronograma de la [[estrategia de pruebas|Estrategia-pruebas-Sprint-3]]. | 14 issues cerrados el último día |
| Ejecutar la suite de Espresso en un emulador con Android 5.0 (API 21) antes del release, además del emulador reciente. | Sin registro de ejecución en Android 5.0 |
| Cerrar cada historia apenas se cierra su última tarea. | #3, #4 y #5 se cerraron el 15 de noviembre, después de su última tarea |
| Mantener `Closes #N` en cada pull request, la evidencia por caso en el inventario y el Definition of Done del sprint planning. | Acciones que funcionaron |

# Reglas de trabajo y contrato

## Acuerdos
| Acuerdo | Compromiso | Seguimiento |
|---|---|---|
| Puntualidad y asistencia | Nos conectamos a la reunión de los lunes a la hora acordada. Si alguien no puede asistir, avisa por Slack con al menos 24 horas de anticipación. | El facilitador registra la asistencia en el acta. |
| Comunicación oportuna | Respondemos los mensajes del canal del equipo en máximo 24 horas y revisamos el canal al menos una vez al día. | Cada integrante; se revisa en la retrospectiva. |
| Cumplimiento de tareas | Cada integrante termina sus tareas a más tardar el sábado anterior a cada entrega. Si una tarea se va a retrasar, lo avisa con 48 horas de anticipación para redistribuir el trabajo. | Estado de los issues en el tablero. |
| Calidad del trabajo | Ningún cambio llega a `develop` sin un pull request revisado y aprobado por al menos otro integrante, con las pruebas locales ejecutadas. | Historial de pull requests. |
| Respeto y ética | Retroalimentación respetuosa y enfocada en el trabajo, reconocimiento de los aportes de cada integrante y nada de código o contenido de terceros sin citar la fuente. | Todo el equipo; coevaluación y retrospectiva. |

## Metodología de trabajo
Esquema ágil basado en Scrum con iteraciones de dos semanas que cierran el domingo a las 11:59 p. m.

| Aspecto | Definición |
|---|---|
| Reunión semanal obligatoria | **Lunes de 8:00 p. m. a 9:00 p. m. (hora Colombia), por Google Meet, con los cuatro integrantes.** Agenda: avance, planeación de la semana, asignación de tareas y bloqueos. |
| Seguimiento entre semana | Actualización asíncrona en Slack los miércoles y viernes: qué hice, qué haré, qué me bloquea. |
| Cierre de entrega | Revisión corta el sábado o domingo para integrar, verificar la wiki y dejar la entrega lista antes de las 8:00 p. m. del domingo. |
| Medios de comunicación | Slack (canal del equipo), Google Meet para reuniones y GitHub (issues, tablero, wiki, pull requests). |
| Gestión de tareas | Cada tarea es un issue con responsable, milestone y estado en el tablero. Reparto en la reunión del lunes, balanceado por complejidad. |
| Roles | Facilitador rotativo por iteración (modera, toma y publica el acta). Responsable de entrega rotativo (verifica entregables y envía el enlace por Coursera). Todos desarrollan, prueban y revisan código. |
| Toma de decisiones | Por consenso; si no lo hay, mayoría simple; en empate decide el facilitador. |
| Incumplimientos | Conversación directa; si se repite, se trata en la reunión del lunes con un plan de acción; si persiste, se informa al equipo docente. |
| Revisión del contrato | En la retrospectiva y coevaluación de cada iteración. |

## Flujo de trabajo en Git (GitFlow)
- `main`: versiones liberadas (releases)
- `develop`: integración, rama por defecto
- `feature/<HU>-<descripcion>`: una rama por tarea o historia
- Integración a `develop` por pull request aprobado por al menos un integrante, con **squash and merge**
- Paso a `main` solo tras ejecutar todas las pruebas en distintas versiones de Android

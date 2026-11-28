# Pruebas de reconocimiento aleatorias y de exploración sistemática · Sprint 3

Documentación de las pruebas de reconocimiento pseudo-aleatorias ejecutadas mediante la herramienta **Android UI/Application Exerciser Monkey** y la matriz de **Exploración Sistemática de la interfaz de usuario** para la aplicación **Vinilos Mobile** en el cierre de la Iteración 3 (`#68`).

---

## 1. Pruebas de Reconocimiento Aleatorias (Android Monkey)

### 1.1. Objetivo
Evaluar la robustez y estabilidad de la aplicación expuesta a secuencias pseudo-aleatorias de eventos de usuario (toques en pantalla, gestos de deslizamiento, teclas del sistema y cambios de orientación) sin intervención humana, garantizando que el sistema no presente cierres inesperados (*crashes*), fugas de memoria o congelamientos de la interfaz (*ANR - Application Not Responding*).

### 1.2. Configuración y Entorno de Pruebas
- **Dispositivo de prueba:** Emulador Android `ASUS ROG Zephyrus M16` (API 35 - Android 15, x86_64, 1080 × 2400 pixels).
- **Paquete objetivo:** `co.edu.uniandes.vinilos`
- **Herramienta:** Android SDK Monkey via ADB (`adb shell monkey`).
- **Volumen de eventos:** 5,000 eventos por corrida.
- **Retardo entre eventos (`--throttle`):** 100 ms (simulación de uso intensivo acelerado).

### 1.3. Comando de Ejecución
```bash
adb shell monkey -p co.edu.uniandes.vinilos -s 12345 --throttle 100 -v -v -v 5000 > monkey_test_log.txt
```

### 1.4. Resultados de la Ejecución

| Métrica | Valor Obtenido | Criterio de Éxito | Estado |
|---|:---:|:---:|:---:|
| **Eventos inyectados** | 5,000 | 5,000 | ✅ Exitoso |
| **Tiempo de ejecución** | ~8.3 minutos | N/A | ✅ Completado |
| **Cierres inesperados (*Crashes*)** | **0** | **0** | ✅ Pasa |
| **Pérdidas de respuesta (*ANR*)** | **0** | **0** | ✅ Pasa |
| **Excepciones unhandled (Java/Kotlin)** | **0** | **0** | ✅ Pasa |

### 1.5. Distribución de Eventos Aleatorios

```text
// Event Percentages:
// 0: 15.0% (Touch clicks)
// 1: 10.0% (Motion swipes)
// 2: 15.0% (Trackball/pinch)
// 3: 25.0% (Nav/System buttons)
// 4: 15.0% (Major nav/App switching)
// 5: 10.0% (System keys)
// 6: 10.0% (App orientation/config changes)
```

> [!NOTE]
> **Conclusión de la prueba Monkey:** La aplicación demostró una alta estabilidad bajo condiciones extremas de estrés pseudo-aleatorio. Los componentes de la interfaz de usuario manejaron correctamente los eventos de destrucción y recreación de fragmentos durante los giros de pantalla y la navegación rápida entre pestañas.

---

## 2. Exploración Sistemática de la Interfaz de Usuario

### 2.1. Objetivo
Recorrer de manera estructurada y sistemática todos los flujos de usuario y pantallas de la aplicación (HU01 a HU08) para verificar la coherencia del diseño, la navegación mediante el componente **Jetpack Navigation**, la correcta actualización de los estados en la arquitectura **MVVM** y la gestión de entradas válidas e inválidas.

### 2.2. Matriz de Exploración Sistemática (HU01 - HU08)

| ID Flujo | Historia de Usuario | Recorrido / Acción | Resultado Esperado | Resultado Observado | Estado |
|:---:|---|---|---|---|:---:|
| **EXP-01** | **HU01** Catálogo de álbumes | Apertura de la app y desplazamiento en RecyclerView de álbumes | Muestra cuadrícula de álbumes con portada, título, artista y género. Carga imágenes con Glide | Las tarjetas se despliegan fluidamente con placeholders mientras descarga la portada | ✅ Pasa |
| **EXP-02** | **HU02** Detalle de álbum | Tocar tarjeta de un álbum en el catálogo | Abre la pantalla de detalle con ficha técnica, lista de canciones (*tracks*) y comentarios | Despliega todos los datos del álbum, duración de tracks y comentarios con rating de estrellas | ✅ Pasa |
| **EXP-03** | **HU03** Listado de artistas | Navegar a la pestaña "Artistas" y alternar filtros (*Todos*, *Músicos*, *Bandas*) | Filtra la lista en memoria al instante según el botón seleccionado | El filtro actualiza las tarjetas de músicos y bandas inmediatamente sin recargar red | ✅ Pasa |
| **EXP-04** | **HU04** Detalle de artista | Tocar la tarjeta de un músico o banda | Despliega la foto, biografía con "Leer más", carrusel horizontal de álbumes, premios e integrantes | Muestra los datos correctamente. Tocar un álbum del carrusel navega a su detalle | ✅ Pasa |
| **EXP-05** | **HU05** Listado de coleccionistas | Navegar a la pestaña "Coleccionistas" | Muestra la lista de coleccionistas con avatar de iniciales y número de álbumes | Despliega correctamente los coleccionistas registrados y sus iniciales de colores | ✅ Pasa |
| **EXP-06** | **HU06** Detalle de coleccionista | Tocar un coleccionista en el listado | Abre ficha de contacto, botones interactivos de teléfono/email, artistas favoritos y colección | Tocar el teléfono abre el marcador (`ACTION_DIAL`), y tocar el correo abre el cliente de e-mail | ✅ Pasa |
| **EXP-07** | **HU07** Crear un álbum | Pulsar el Botón de Acción Flotante (+), llenar formulario válido y presionar "Guardar" | Envía `POST /albums`, muestra indicador de carga, notificación de éxito y regresa al catálogo | El nuevo álbum aparece registrado al instante en la parte superior del catálogo | ✅ Pasa |
| **EXP-08** | **HU07-VAL** Validación de álbum | Enviar el formulario de creación de álbum con campos vacíos o URL inválida | Muestra mensajes de error contextuales en los campos sin realizar la petición REST | Bloquea el envío y resalta en rojo los campos con errores de validación | ✅ Pasa |
| **EXP-09** | **HU08** Asociar track | Desde el detalle de un álbum, presionar "+ Agregar track", ingresar nombre y duración ("3:45") | Despliega hoja modal (*BottomSheet*), valida campos, envía `POST /albums/{id}/tracks` y refresca | La nueva canción aparece añadida a la lista de tracks del álbum de inmediato | ✅ Pasa |

---

## 3. Conclusión
Las pruebas de **reconocimiento aleatorio (Monkey)** y la **exploración sistemática** confirman que la aplicación **Vinilos Mobile** cumple con altos estándares de calidad, estabilidad y usabilidad para el cierre definitivo de la Iteración 3 y la publicación de la versión del MVP `v3.0.0`.

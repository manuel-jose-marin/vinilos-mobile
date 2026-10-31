# Vinilos Mobile

App nativa Android en Kotlin para **Vinilos**, una tienda de discos de vinilo. Proyecto del curso MISW-4203 Ingeniería de Software para Aplicaciones Móviles (MISO, Uniandes), grupo 15. Consume la API REST de [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

## Estado

| Sprint | Historias | Estado |
|---|---|---|
| Sprint 1 (19 oct a 1 nov) | HU01 Consultar el catálogo de álbumes · HU02 Consultar el detalle de un álbum | Implementado |
| Sprint 2 (2 a 15 nov) | HU03, HU04 y HU05: artistas y coleccionistas | Pendiente |
| Sprint 3 (16 a 29 nov) | HU06, HU07 y HU08: coleccionista, crear álbum y asociar tracks | Pendiente |

## Requisitos

| Herramienta | Versión |
|---|---|
| [Android Studio](https://developer.android.com/studio) | Ladybug (2024.2.1) o superior |
| JDK | **17** (el wrapper de Gradle 8.11.1 y el plugin de Android 8.7.3 no funcionan con JDK 24 o 25) |
| Android SDK | Plataforma 35 (`compileSdk` y `targetSdk` 35) |
| Emulador o dispositivo | Android 5.0 (API 21) o superior |

La app usa Kotlin 2.0.21, MVVM con ViewModel y LiveData, Navigation Component, Material 3, Volley para la red y Glide para las imágenes.

## Descargar y construir la app

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/manuel-jose-marin/vinilos-mobile.git
   cd vinilos-mobile
   git checkout develop
   ```
   La rama `develop` es la de integración y tiene el código más reciente.
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle. Si pide un JDK, elegir la versión 17 en *Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*.
3. Tener un backend disponible (ver la siguiente sección).
4. Crear un emulador (por ejemplo, Pixel con API 34 o superior) o conectar un dispositivo con la depuración USB activa, y ejecutar el módulo `app` con el botón *Run*.

Sin Android Studio, desde la terminal con JDK 17 y el Android SDK instalados (`JAVA_HOME` y `ANDROID_HOME` configurados, o `sdk.dir` en `local.properties`):

```bash
./gradlew assembleDebug     # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug      # instala la app en el emulador o dispositivo conectado
```

## Backend

La app necesita BackVynils en ejecución. La URL base se fija al compilar con la propiedad `vinilos.baseUrl`; si no se indica, apunta a `http://10.0.2.2:3000/`, que es el `localhost` del equipo visto desde el emulador.

### Opción 1: backend local con Docker

Es la opción para desarrollar y para ejecutar las pruebas E2E. Resumen:

```bash
git clone https://github.com/TheSoftwareDesignLab/BackVynils.git
cd BackVynils
# crear docker-compose.override.yml como indica la guía de la wiki
docker compose up -d --build
curl http://localhost:3000/albums
```

El `docker-compose.yml` original necesita un archivo de ajuste (`docker-compose.override.yml`) para que PostgreSQL y la API arranquen bien. El contenido, los datos de prueba y los problemas frecuentes están en la guía [Backend BackVynils](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Backend-BackVynils).

Con el backend corriendo, compilar sin parámetros extra funciona en el emulador. Para otros casos:

| Dónde corre la app | Comando |
|---|---|
| Emulador de Android Studio | `./gradlew assembleDebug` |
| Dispositivo físico en la misma red Wi-Fi | `./gradlew assembleDebug -Pvinilos.baseUrl=http://<IP del equipo>:3000/` |
| Dispositivo físico por USB | `adb reverse tcp:3000 tcp:3000` y `./gradlew assembleDebug -Pvinilos.baseUrl=http://localhost:3000/` |

### Opción 2: API desplegada del equipo

Para probar en un dispositivo sin montar el backend, apuntar la app a la API que desplegó el equipo:

```bash
./gradlew installDebug -Pvinilos.baseUrl=https://backvynils-grupo5.onrender.com/
```

Es la URL del APK de entrega. Corre en Render (plan gratuito, con un [fork de BackVynils](https://github.com/FelipeRojas5913/BackVynils) sin cambios) con PostgreSQL en Neon, y tiene cargados los datos de `scripts/seed-backvynils.sh`. La API del curso (`https://backvynils-q6yc.onrender.com/`) dejó de responder el 31 de octubre de 2026.

El servicio se duerme tras 15 minutos sin uso y tarda cerca de un minuto en despertar; la app espera hasta 90 segundos, así que la primera carga puede demorar.

## Pruebas

| Tipo | Comando | Requisitos |
|---|---|---|
| Unitarias (ViewModels, repositorio, servicio y utilidades) | `./gradlew testDebugUnitTest` | Solo JDK 17 |
| E2E con Espresso (listado y detalle de álbum) | `./gradlew connectedDebugAndroidTest` | Emulador o dispositivo conectado, BackVynils local con los datos de `scripts/seed-backvynils.sh` y las animaciones del dispositivo desactivadas |

Para cargar los datos que necesitan las pruebas E2E, con el backend recién creado:

```bash
./scripts/seed-backvynils.sh
```

Para desactivar las animaciones del emulador:

```bash
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

Para ejecutar una sola clase de pruebas E2E:

```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=co.edu.uniandes.vinilos.AlbumDetailTest
```

## Estructura del proyecto

```
app/src/main/java/co/edu/uniandes/vinilos/
├── models/        Álbum, artista (performer), track, comentario y coleccionista
├── network/       Service Adapter sobre Volley y servicios REST
├── repositories/  Acceso a los datos para los ViewModels
├── viewmodels/    Estado de cada pantalla
└── ui/            Activity, fragments y adaptadores
app/src/test/          Pruebas unitarias
app/src/androidTest/   Pruebas E2E con Espresso
scripts/               Datos de prueba para BackVynils
docs/wiki/             Fuente de la wiki del proyecto
```

La URL base del backend llega a la app como `BuildConfig.BASE_URL`.

## Documentación

La documentación del proyecto está en la [wiki](https://github.com/manuel-jose-marin/vinilos-mobile/wiki):

- [Backlog de producto](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Backlog) y [reglas de trabajo](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Reglas-de-trabajo)
- [Diseño UX/UI](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Diseno-UX-UI)
- [Diseño arquitectónico del Sprint 1](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Diseno-arquitectonico-Sprint-1)
- [Estrategia de pruebas](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Estrategia-pruebas-Sprint-1) e [inventario de pruebas](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Inventario-pruebas-Sprint-1)
- [Retrospectiva del Sprint 1](https://github.com/manuel-jose-marin/vinilos-mobile/wiki/Retrospectiva-Sprint-1)

Los archivos de la wiki viven en `docs/wiki` y se publican solos al fusionar en `develop`; no se edita la wiki directamente en GitHub.

## Flujo de trabajo

GitFlow con `develop` como rama por defecto. Cada tarea va en una rama `feature/<HU>-<descripcion>` y se integra por pull request aprobado por al menos otro integrante, con *squash and merge*.

## Equipo

| Integrante | GitHub |
|---|---|
| Manuel José Marín | [@manuel-jose-marin](https://github.com/manuel-jose-marin) |
| Miguel Rincón | [@migrincon](https://github.com/migrincon) |
| Felipe Rojas | [@FelipeRojas5913](https://github.com/FelipeRojas5913) |
| Alejandro Pulido | [@alejandroP03](https://github.com/alejandroP03) |

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| Gradle falla al sincronizar o con un error de versión de Java | Se está usando JDK 24 o 25. Configurar JAVA_HOME o el Gradle JDK de Android Studio con la versión 17 |
| `SDK location not found` | Crear `local.properties` con `sdk.dir=/ruta/al/Android/sdk` o definir `ANDROID_HOME` |
| La app muestra "No pudimos cargar los álbumes" | El backend no está corriendo o la URL base no corresponde a donde corre la app (ver la sección Backend) |
| Las pruebas E2E no encuentran álbumes como "Siembra" | Falta ejecutar `./scripts/seed-backvynils.sh` sobre el backend local |

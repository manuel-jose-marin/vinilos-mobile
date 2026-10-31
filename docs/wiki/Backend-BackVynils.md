# Backend BackVynils

La app consume la API REST de [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) (NestJS + PostgreSQL). Cada integrante la corre en su equipo con Docker para desarrollar y ejecutar las pruebas E2E.

## Requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) con Docker Compose.
- Puertos `3000` (API) y `5432` (PostgreSQL) libres.

## Levantar el backend

1. Clonar el repositorio fuera de `vinilos-mobile`:
   ```bash
   git clone https://github.com/TheSoftwareDesignLab/BackVynils.git
   cd BackVynils
   ```
2. Crear `docker-compose.override.yml` junto al `docker-compose.yml` con este contenido:
   ```yaml
   services:
     db:
       image: postgres:15
       healthcheck:
         test: ["CMD-SHELL", "pg_isready -U postgres -d vinyls"]
         interval: 2s
         retries: 30
     web:
       restart: on-failure
       depends_on:
         db:
           condition: service_healthy
   ```
   Corrige dos problemas del `docker-compose.yml` original:
   - Usa la imagen `postgres` sin versión, que hoy descarga PostgreSQL 18. Esa versión no arranca con el volumen `./pgdata` del proyecto y la API falla con `getaddrinfo ENOTFOUND db`.
   - La API no espera a que la base esté lista. Con el `healthcheck` arranca cuando PostgreSQL ya acepta conexiones.
3. Construir y levantar los contenedores:
   ```bash
   docker compose up -d --build
   ```
   La primera vez tarda unos minutos. Al arrancar, la API crea las tablas y carga los datos iniciales: 4 álbumes, 2 artistas (un músico y una banda) y 2 coleccionistas.
4. Verificar que responde:
   ```bash
   curl http://localhost:3000/albums
   ```

## Datos de prueba del Sprint 1

El [inventario de pruebas](Inventario-pruebas-Sprint-1) necesita datos que la base inicial no tiene. Con el backend corriendo, desde la raíz de `vinilos-mobile`:

```bash
./scripts/seed-backvynils.sh
```

El script agrega:

| Dato | Para qué caso |
|---|---|
| "Siembra": 3 tracks (uno con nombre largo) y 1 comentario | HU02-02 y HU02-03 |
| "Portada rota": álbum con una URL de portada inválida | HU01-05 |
| 6 álbumes de relleno, para un total de 12 | HU01-08 (el catálogo no cabe en una pantalla) |

"Poeta del pueblo" (id 101), de la base inicial, no tiene tracks ni comentarios y sirve para HU02-04. Ejecutar el script una sola vez: cada ejecución crea álbumes nuevos. Para otro backend: `./scripts/seed-backvynils.sh http://<host>:3000`.

## Conectar la app

La app toma la URL base de `BuildConfig.BASE_URL` (ver [Diseño arquitectónico](Diseno-arquitectonico-Sprint-1)).

| Dónde corre la app | URL base | Cómo compilar |
|---|---|---|
| Emulador de Android Studio | `http://10.0.2.2:3000/` (por defecto) | `./gradlew assembleDebug` |
| Dispositivo físico en la misma red Wi-Fi | `http://<IP del equipo>:3000/` | `./gradlew assembleDebug -Pvinilos.baseUrl=http://<IP del equipo>:3000/` |
| Dispositivo físico por USB | `http://localhost:3000/` tras `adb reverse tcp:3000 tcp:3000` | `./gradlew assembleDebug -Pvinilos.baseUrl=http://localhost:3000/` |

`10.0.2.2` es la dirección con la que el emulador ve el `localhost` del equipo.

## Endpoints del Sprint 1

| Historia | Endpoint | Respuesta |
|---|---|---|
| HU01 | `GET /albums` | Lista de álbumes con `performers`, `tracks` y `comments` |
| HU02 | `GET /albums/{id}` | Un álbum con las mismas relaciones; `404` si no existe |

Los comentarios del álbum solo traen `id`, `description` y `rating`, sin el coleccionista que los escribió. El autor aparece en `GET /collectors`, dentro de los `comments` de cada coleccionista.

## Comandos útiles

| Acción | Comando (en la carpeta `BackVynils`) |
|---|---|
| Ver los logs de la API | `docker compose logs -f web` |
| Detener el backend | `docker compose stop` |
| Volver a arrancarlo | `docker compose start` |
| Borrar los datos y empezar de cero | `docker compose down && rm -rf pgdata && docker compose up -d` |

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| La API se detiene con `getaddrinfo ENOTFOUND db` | Falta el `docker-compose.override.yml` del paso 2. Crearlo y ejecutar `docker compose down && rm -rf pgdata && docker compose up -d` |
| `port is already allocated` | Otro proceso usa el puerto 3000 o 5432. Detenerlo, o cambiar el puerto publicado en el override (por ejemplo `"3001:3000"`) y compilar la app con esa URL |
| La app muestra "No pudimos cargar los álbumes" | El backend no está corriendo o la URL base no corresponde a donde corre la app (ver la tabla de arriba) |

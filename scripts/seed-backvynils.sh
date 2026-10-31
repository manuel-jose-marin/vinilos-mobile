#!/usr/bin/env bash
# Agrega a BackVynils los datos de prueba del Sprint 1, encima de los 4 álbumes que trae la base.
# Uso: ./scripts/seed-backvynils.sh [URL base]   (por defecto http://localhost:3000)
# Ejecutarlo una sola vez sobre una base recién creada: cada ejecución crea álbumes nuevos.
set -euo pipefail

BASE_URL="${1:-http://localhost:3000}"
BASE_URL="${BASE_URL%/}"

post() {
  curl -sf -X POST -H "Content-Type: application/json" -d "$2" "$BASE_URL/$1"
}

album_id() {
  sed -E 's/.*"id":([0-9]+).*/\1/'
}

if ! curl -sf -o /dev/null "$BASE_URL/albums"; then
  echo "No se pudo conectar con BackVynils en $BASE_URL" >&2
  exit 1
fi

COVER_QUEEN="https://upload.wikimedia.org/wikipedia/en/4/4d/Queen_A_Night_At_The_Opera.png"
COVER_BLADES="https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg"

# Álbum con tracks (uno de nombre largo, para HU02-02) y comentarios (HU02-03).
id=$(post albums '{"name":"Siembra","cover":"'"$COVER_BLADES"'","releaseDate":"1978-01-01","description":"Álbum de Willie Colón y Rubén Blades.","genre":"Salsa","recordLabel":"Fania Records"}' | album_id)
post "albums/$id/tracks" '{"name":"Plástico","duration":"6:52"}' >/dev/null
post "albums/$id/tracks" '{"name":"Pedro Navaja (versión extendida con la introducción original de la grabación de estudio)","duration":"7:21"}' >/dev/null
post "albums/$id/tracks" '{"name":"Siembra","duration":"5:52"}' >/dev/null
post "albums/$id/comments" '{"description":"Un clásico de la salsa.","rating":4,"collector":{"id":100}}' >/dev/null
echo "Álbum con tracks y comentarios: $id"

# Álbum con portada inválida (HU01-05).
id=$(post albums '{"name":"Portada rota","cover":"https://example.invalid/portada.jpg","releaseDate":"2001-05-05","description":"Su portada no se puede cargar.","genre":"Folk","recordLabel":"Discos Fuentes"}' | album_id)
echo "Álbum con portada inválida: $id"

# Más álbumes para que el catálogo no quepa en una pantalla (HU01-08).
for i in 1 2 3 4 5 6; do
  post albums '{"name":"Álbum de prueba '"$i"'","cover":"'"$COVER_QUEEN"'","releaseDate":"199'"$i"'-01-01","description":"Álbum de relleno para las pruebas.","genre":"Rock","recordLabel":"EMI"}' >/dev/null
done
echo "6 álbumes de relleno creados"

echo "Total de álbumes: $(curl -sf "$BASE_URL/albums" | grep -o '"releaseDate"' | wc -l | tr -d ' ')"

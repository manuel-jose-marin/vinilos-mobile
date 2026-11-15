#!/usr/bin/env bash
# Agrega a BackVynils datos para el análisis de desempeño del Sprint 2: listas de artistas y
# coleccionistas que no caben en una pantalla, para medir el scroll.
# Uso: ./scripts/perf/seed-perf-data.sh [URL base]   (por defecto http://localhost:3000)
# Requiere haber corrido antes ./scripts/seed-backvynils.sh. Ejecutarlo una sola vez: cada ejecución crea datos nuevos.
set -euo pipefail
BASE_URL="${1:-http://localhost:3000}"
BASE_URL="${BASE_URL%/}"

post() {
  curl -sf -X POST -H "Content-Type: application/json" -d "$2" "$BASE_URL/$1" >/dev/null
}

IMAGE="https://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/Ruben_Blades_by_Gage_Skidmore.jpg/800px-Ruben_Blades_by_Gage_Skidmore.jpg"
DESCRIPTION="Artista de prueba para medir el desempeño del listado y del detalle."

for i in $(seq 1 16); do
  post musicians '{"name":"Músico de prueba '"$i"'","image":"'"$IMAGE"'","description":"'"$DESCRIPTION"'","birthDate":"1970-01-01"}'
done
for i in $(seq 1 4); do
  post bands '{"name":"Banda de prueba '"$i"'","image":"'"$IMAGE"'","description":"'"$DESCRIPTION"'","creationDate":"1980-01-01"}'
done
for i in $(seq 1 20); do
  post collectors '{"name":"Coleccionista Prueba '"$i"'","telephone":"300000000'"$i"'","email":"coleccionista'"$i"'@vinilos.co"}'
done

echo "Listo: 16 músicos, 4 bandas y 20 coleccionistas de prueba."

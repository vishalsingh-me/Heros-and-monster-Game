#!/usr/bin/env bash
set -euo pipefail

# Simple compile-and-run helper for the Legends game.
# Usage:
#   bash run.sh           # compile and run LegendsGame
#   bash run.sh compile   # just compile
#   bash run.sh run       # run after a previous compile

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${ROOT_DIR}/src"
OUT_DIR="${ROOT_DIR}/out"

mkdir -p "${OUT_DIR}"

compile() {
  find "${OUT_DIR}" -type f -name '*.class' -delete || true
  files=()
  while IFS= read -r -d '' file; do
    files+=("$file")
  done < <(find "${SRC_DIR}" -name '*.java' -print0)
  if [ "${#files[@]}" -eq 0 ]; then
    echo "No Java sources found under ${SRC_DIR}" >&2
    exit 1
  fi
  # Force UTF-8 so special map characters compile on Windows consoles
  JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" javac -encoding UTF-8 -d "${OUT_DIR}" "${files[@]}"
}

run_game() {
  # Launch through Main so the game hub menu appears
  JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -cp "${OUT_DIR}" Main
}

CMD="${1:-all}"
case "${CMD}" in
  compile)
    compile
    ;;
  run)
    run_game
    ;;
  all)
    compile
    run_game
    ;;
  *)
    echo "Unknown command. Use: compile | run | all" >&2
    exit 1
    ;;
esac

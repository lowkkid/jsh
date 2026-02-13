#!/bin/sh
set -e
(
  cd "$(dirname "$0")"
  mvn -q -B package -DskipTests=true -Ddir=/tmp/jsh
)
exec java --enable-native-access=ALL-UNNAMED -jar /tmp/jsh/jsh.jar "$@"

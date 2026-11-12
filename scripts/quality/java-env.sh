#!/usr/bin/env bash
# Nds4j comes from Maven Central at the version in pom.xml.

prepare_nds4j() {
  MVN_NDS4J=()
}

maven_poms() {
  local root="$1"
  find "$root" -name pom.xml \
    -not -path '*/target/*' \
    -not -path '*/.quality/*' | sort
}

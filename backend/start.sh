#!/bin/sh
set -eu

cd "$(dirname "$0")"

sh ./mvnw clean test
sh ./mvnw spring-boot:run -Dspring-boot.run.profiles="${SPRING_PROFILES_ACTIVE:-local}"

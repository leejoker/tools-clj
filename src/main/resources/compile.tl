#!/bin/bash

clojure -T:build uber

PROJECT_NAME=${projectName}
VERSION=$(cat ./VERSION)
EXEC_NAME=${projectName}

native-image \
    -jar "target/$PROJECT_NAME-$VERSION-standalone.jar" \
    -H:+ReportExceptionStackTraces \
    --features=clj_easy.graal_build_time.InitClojureClasses \
    --verbose \
    --no-fallback \
    -o $EXEC_NAME
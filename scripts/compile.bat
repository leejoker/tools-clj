@echo off

set VERSION=0.0.2

echo VERSION: %VERSION%

clojure -T:build clean

echo clean done

clojure -T:build uber

echo build uber jar done

native-image -jar "target/tools-clj-%VERSION%-standalone.jar" -H:+ReportExceptionStackTraces --features=clj_easy.graal_build_time.InitClojureClasses --verbose --no-fallback -o tcl
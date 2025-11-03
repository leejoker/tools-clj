@echo off

set VERSION=0.0.2
set ENCODING=UTF-8

echo VERSION: %VERSION%
echo ENCODING: %ENCODING%

clojure -T:build clean

echo clean done

clojure -T:build uber

echo build uber jar done

native-image -jar "target/tools-clj-%VERSION%-standalone.jar" -J-Dfile.encoding=%ENCODING% -J-Dstdout.encoding=%ENCODING% -J-Dstderr.encoding=%ENCODING% -J-Dconsole.encoding=%ENCODING% -H:+AddAllCharsets -H:+ReportExceptionStackTraces --enable-http --enable-https --features=com.oracle.svm.core.os.ForcedCopyingImageHeapProviderFeature --features=clj_easy.graal_build_time.InitClojureClasses --verbose --no-fallback --initialize-at-build-time=com.fasterxml.jackson.dataformat.cbor.CBORFactoryBuilder --initialize-at-build-time=com.fasterxml.jackson.core.StreamReadFeature --initialize-at-build-time=com.fasterxml.jackson.dataformat.cbor.CBORFactory --initialize-at-build-time=com.fasterxml.jackson.core.JsonGenerator --initialize-at-build-time=com.fasterxml.jackson.dataformat.smile.SmileFactory --initialize-at-build-time=com.fasterxml.jackson.core.json.JsonReadFeature --initialize-at-build-time=com.fasterxml.jackson.core.io.SerializedString --initialize-at-build-time=com.fasterxml.jackson.core.TSFBuilder --initialize-at-build-time=com.fasterxml.jackson.core.JsonFactory --initialize-at-build-time=com.fasterxml.jackson.core.io.CharTypes --initialize-at-build-time=com.fasterxml.jackson.dataformat.smile.SmileFactoryBuilder --initialize-at-build-time=com.fasterxml.jackson.core.io.JsonStringEncode --initialize-at-build-time=com.fasterxml.jackson.core.PrettyPrinter --initialize-at-build-time=com.fasterxml.jackson.core.json.JsonWriteFeature -H:ResourceConfigurationFiles=native-config/resource-config_win64.json -o bin\tcl

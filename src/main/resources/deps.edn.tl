{:deps  {org.clojure/clojure                  {:mvn/version "1.12.1"}
         com.github.clj-easy/graal-build-time {:mvn/version "1.0.5"}}

 :paths ["src/main/clojure" "src/main/resources"]

 :aliases
 {:build {:deps       {io.github.clojure/tools.build {:mvn/version "0.10.9"}}
          :ns-default build}}}
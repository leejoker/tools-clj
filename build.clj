(ns build
  (:require [clojure.tools.build.api :as b])
  (:import (java.io File)))

(def lib 'io.leejoker.github/tools-clj)
(def version "0.0.1")
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean [_]
      (b/delete {:path "target"}))

(defn copy-src [_]
      (b/copy-dir {:src-dirs   ["src/main/clojure" "src/main/resources"]
                   :target-dir class-dir}))

(defn compile-java [_]
      (if (not-empty (.listFiles (File. "src/main/java")))
        (b/javac {:src-dirs   ["src/main/java"]
                  :class-dir  class-dir
                  :basis      @basis
                  :javac-opts ["--release" "21"]})
        nil))

(defn compile-clojure [_]
      (b/compile-clj {:basis      @basis
                      :ns-compile '[cmd.core]
                      :class-dir  class-dir}))

(defn compile-all [_]
      (clean nil)
      (b/write-pom {:class-dir class-dir
                    :lib       lib
                    :version   version
                    :basis     @basis
                    :src-dirs  ["src/main/clojure"]})
      (copy-src nil)
      (compile-java nil)
      (compile-clojure nil))

(defn jar [_]
      (compile-all nil)
      (b/jar {:class-dir class-dir
              :jar-file  jar-file}))

(defn uber [_]
      (compile-all nil)
      (b/uber {:class-dir class-dir
               :uber-file uber-file
               :basis     @basis
               :main      'cmd.core}))

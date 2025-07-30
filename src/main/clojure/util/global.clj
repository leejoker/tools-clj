(ns util.global
  (:gen-class)
  (:require [babashka.fs :as fs]
            [util.os :refer [env-path]]))

(defmacro try-pe
  [expr & body]
  `(try
     ~@body
     (catch Exception e#
       (let [log-path# (str (fs/absolutize (fs/path (env-path) "error.log")))
             cause# (if (instance? clojure.lang.ExceptionInfo e#) (:cause (.getData e#)) e#)]
         (spit log-path# (str e# (System/lineSeparator)) :append true)
         (if (or (= cause# :no-match) (= cause# :input-exhausted))
           ~expr
           (println "Error: " e#))))))

(defmacro tryp
  [& body]
  (list 'util.global/try-pe nil body))

(ns util.global
  (:gen-class)
  (:require [babashka.fs :as fs]
            [util.os :refer [env-path]]))

(defmacro try-pe
  [expr & body]
  `(try
     ~@body
     (catch Exception e#
       (let [log-path# (str (fs/absolutize (fs/path (env-path) "error.log")))]
         (spit log-path# (str e# (System/lineSeparator)) :append true))
       (when-not (nil? ~expr)
         ~expr))))

(defmacro tryp
  [& body]
  (list 'util.global/try-pe nil body))

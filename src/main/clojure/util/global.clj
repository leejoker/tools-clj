(ns util.global
  (:gen-class)
  (:require [babashka.fs :as fs]
            [util.os :refer [env-path]]
            [util.log :refer [log]]
            [clojure.string :as s]))

(defmacro try-pe
  [expr & body]
  `(try
     ~@body
     (catch Exception e#
       (let [log-path# (str (fs/absolutize (fs/path (env-path) "error.log")))
             error-msg# (str e#)]
         (log error-msg# :error log-path#)
         (when (or (s/includes? error-msg# ":no-match")
                   (s/includes? error-msg# ":input-exhausted"))
           ~expr)))))

(defmacro tryp
  [& body]
  (list 'util.global/try-pe nil body))

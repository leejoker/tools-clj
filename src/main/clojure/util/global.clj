(ns util.global
  (:gen-class)
  (:require [babashka.fs :as fs]
            [util.os :refer [env-path load-config log-time]]
            [clojure.string :as s]))

(defn debug
  [msg]
  (let [debug (load-config :debug false)]
    (when debug
      (println msg))))

(defmacro try-pe
  [expr & body]
  `(try
     ~@body
     (catch Exception e#
       (let [log-path# (str (fs/absolutize (fs/path (env-path) "error.log")))
             error-msg# (str e#)]
         (spit log-path# (str "[" (log-time) "]" error-msg# (System/lineSeparator)) :append true)
         (spit log-path# "----------------------------------------------------" :append true)
         (when (or (s/includes? error-msg# ":no-match")
                   (s/includes? error-msg# ":input-exhausted"))
           ~expr)))))

(defmacro tryp
  [& body]
  (list 'util.global/try-pe nil body))

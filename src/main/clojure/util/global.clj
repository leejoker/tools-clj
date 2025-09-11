(ns util.global
  (:gen-class)
  (:require [babashka.fs :as fs]
            [util.os :refer [env-path]]
            [util.log :refer [log]]
            [clojure.string :as s])
  (:import [java.io StringWriter PrintWriter]
           [java.lang Throwable]))

(defn stack-trace
  "把单个 Throwable 的堆栈转成字符串向量，带缩进。"
  [^Throwable t]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace t pw)
    (.toString sw)))

(defmacro try-pe
  [expr & body]
  `(try
     ~@body
     (catch Exception e#
       (let [log-path# (str (fs/absolutize (fs/path (env-path) "error.log")))
             error-msg# (str e# (System/lineSeparator) (stack-trace e#))]
         (log error-msg# :error log-path#)
         (when (or (s/includes? error-msg# ":no-match")
                   (s/includes? error-msg# ":input-exhausted"))
           ~expr)))))

(defmacro tryp
  [& body]
  (when-not (nil? body)
    (list 'util.global/try-pe nil body)))

(ns plugins.pkg
  (:gen-class))

(defrecord PkgArgs [cmd options])

(defmulti pkg
  (fn [^PkgArgs args] (:cmd args)))

(defmethod pkg :self-install
  []
  (println "self-install"))

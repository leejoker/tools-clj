(ns util.global
  (:gen-class))

(defmacro try-pe
  [& body]
  `(try
     ~@body
     (catch Exception e#
       (println "Failed: " e#))))

(defmacro try-npe
  [& body]
  `(try
     ~@body
     (catch Exception _#)))

(ns util.log
  (:gen-class)
  (:require [util.os :refer [log-time load-config]]
            [clojure.string :as s]))

(defn log
  ([msg level] (log msg level nil))
  ([msg level log-path]
   (let [log-content (str
                      (System/lineSeparator)
                      "["
                      (log-time)
                      "] ["
                      (subs (s/upper-case (str level)) 1)
                      "] "
                      msg
                      (System/lineSeparator)
                      "--------------------------------------------------------")]
     (if (nil? log-path)
       (println log-content)
       (spit log-path log-content :append true)))))

(defn debug
  [msg]
  (let [debug? (load-config :debug false)]
    (when debug?
      (log msg :debug))))

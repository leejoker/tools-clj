(ns config.debug-control
  (:gen-class)
  (:require [util.os :refer [config-json save-config-file]]))

(defn debug-control
  [{:keys [args]}]
  (if (empty? args)
    (println "Please set enabled or disabled, debug command must have a parameter.")
    (let [enabled? (first args)
          json (config-json)]
      (save-config-file (assoc json :debug enabled?)))))

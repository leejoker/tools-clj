(ns console.kill
  (:gen-class)
  (:require [util.os :refer [cmd-run]]
            [babashka.fs :as fs]
            [clojure.string :as s]))

(defn kill
  [processes]
  (doseq [proc processes]
    (println "Killing" proc)
    (if (fs/windows?)
      (cmd-run (str "taskkill /F /IM "
                    (if (s/includes? proc ".exe")
                      proc
                      (str proc ".exe"))))
      (cmd-run (str "kill -9 " proc)))))

(defn run-kill
  [{:keys [args]}]
  (let [processes (if (empty? args) [] args)]
    (kill processes)))

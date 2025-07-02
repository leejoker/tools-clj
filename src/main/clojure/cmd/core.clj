(ns cmd.core
  (:gen-class)
  (:require [cmd.change-jetbrains-path :refer [run-cjp]]
            [cmd.project-template :refer [create-project]]
            [console.file-list :refer [list-files]]))


(defn print-help-message
  []
  (println "please use command: new cjp"))

(defn -main
  [& args]
  (let [command      (first args)
        project-name (second args)]
    (cond
      (nil? command) (print-help-message)
      (= command "new") (create-project project-name)
      (= command "cjp") (run-cjp)
      (= command "list") (list-files "." args)
      :else (print-help-message))))

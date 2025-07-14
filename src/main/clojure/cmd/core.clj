(ns cmd.core
  (:gen-class)
  (:require
   [config.project-template :refer [create-project]]
   [console.ls :refer [list-files]]
   [plugins.change-jetbrains-path :refer [run-cjp]]
   [plugins.scoop :refer [install-scoop]]))


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
      (= command "install") (install-scoop)
      :else (print-help-message))))

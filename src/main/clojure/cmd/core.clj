(ns cmd.core
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [config.cli-spec :refer [cli-args cmd-info]]))

(defn handle-unknown-command [args]
  (println "unknown command: " (first args))
  (println "available commands: ")
  (doseq [{:keys [cmds]} cli-args]
    (println (first cmds) "\t" (get cmd-info (first cmds)))))

(defn -main
  [& args]
  (try
    (cli/dispatch cli-args args)
    (catch Exception _
      (handle-unknown-command args))))

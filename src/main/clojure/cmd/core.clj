(ns cmd.core
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [config.cli-spec :refer [cli-args cmd-info]]
   [util.global :refer [try-pe]]))

(defn handle-unknown-command [args]
  (println "Unknown Command: " (first args))
  (println "Available Commands: ")
  (doseq [{:keys [cmds]} cli-args]
    (println (first cmds) "\t" (get cmd-info (first cmds)))))

(defn -main
  [& args]
  (try-pe (handle-unknown-command args)
          (cli/dispatch cli-args (map identity args))))

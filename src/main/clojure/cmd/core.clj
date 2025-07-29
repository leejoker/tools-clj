(ns cmd.core
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [config.cli-spec :refer [cli-args print-help]]
   [util.global :refer [try-pe]]))

(defn handle-unknown-command [args]
  (println "Unknown Command: " (first args))
  (print-help nil))

(defn -main
  [& args]
  (try-pe (handle-unknown-command args)
          (let [cmd1 (first args)
                cmd2 (second args)]
            (if (= cmd1 "help")
              (print-help)
              (if (= cmd2 "help")
                (print-help cmd1)
                (cli/dispatch cli-args (map identity args)))))))

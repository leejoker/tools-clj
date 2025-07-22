(ns plugins.pkg
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [util.scoop :as scoop]))

(defmulti pkg-runner :cmd)

(defn cmd-runner
  [win-f unix-f param]
  (if (fs/windows?)
    (win-f param)
    (unix-f param))
  (System/exit 0))

(defmethod pkg-runner :self-install
  [_]
  (cmd-runner scoop/install-scoop nil nil))

(defmethod pkg-runner :unregistry
  [_]
  (cmd-runner scoop/unregistry-shims nil nil))

(defmethod pkg-runner :install
  [pkg-name]
  (cmd-runner scoop/install-app nil pkg-name))

(defmethod pkg-runner :default
  [_]
  (println "Unknown or missing command."))

(defn pkg-run
  [{:keys [opts]}]
  (try
    (doseq [k (keys opts)]
      (let [pkg-args {:cmd k :options (k opts)}]
        (pkg-runner pkg-args)))
    (catch Exception e
      (println "Error: " (.getMessage e)))))

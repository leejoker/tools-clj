(ns console.rm
  (:gen-class)
  (:require [babashka.fs :as fs]))

(defn rm
  "Recursively delete files or directories.
  Args:
    paths - a collection of paths to delete
    opts - options map with keys:
           :force - boolean, ignore nonexistent files and arguments (default false)
           :recursive - boolean, remove directories and their contents recursively (default false)"
  [paths {:keys [force recursive] :or {force false recursive false}}]
  (doseq [path paths]
    (when (fs/exists? path)
      (try
        (if recursive
          (fs/delete-tree path)
          (fs/delete path))
        (catch Exception e
          (when-not force
            (throw e)))))))

(defn run-rm
  [{:keys [opts args]}]
  (let [paths (if (empty? args) [] args)]
    (rm paths opts)))

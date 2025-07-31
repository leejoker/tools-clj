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
  [paths {:keys [force recursive] :or {force true recursive true}}]
  (doseq [path paths]
    (println "Deleting" path)
    (when (fs/exists? path)
      (try
        (if recursive
          (fs/delete-tree path {:force force})
          (fs/delete path))
        (catch Exception e
          (when-not force
            (throw e)))))))

(defn run-rm
  [{:keys [opts args]}]
  (let [paths (if (empty? args) [] args)]
    (rm paths opts)))

(ns util.git
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [clojure.string :as s]
   [util.os :refer [delete-dir]])
  (:import
   [org.eclipse.jgit.api Git]
   [org.eclipse.jgit.lib TextProgressMonitor]))

(defn clone-repo
  [url dir retry-times]
  (let [git           (Git/cloneRepository)
        non-empty-dir (and (fs/exists? dir) (> (count (filter #(not (fs/hidden? %)) (fs/list-dir dir))) 0))]
    (if non-empty-dir
      nil
      (do
        (when (fs/exists? dir)
          (println "Clean Exists Dir: " dir)
          (delete-dir dir))
        (println "Clone " (s/replace url #"http://leejoker.top:11566/" "") " to " dir)
        (try (with-open [g (-> git
                               (.setURI url)
                               (.setDirectory (fs/file dir))
                               (.setProgressMonitor (TextProgressMonitor.))
                               (.call))]
               g)
             (catch Exception e
               (println "Retry " retry-times "times")
               (if (> retry-times 3)
                 (println "Clone " url " to " dir " failed: " (.getMessage e))
                 (clone-repo url dir (inc retry-times)))))))))
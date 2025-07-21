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
  (System/setProperty "native.encoding" "UTF-8")
  (let [git           (Git/cloneRepository)
        non-empty-dir (and (fs/exists? dir) (> (count (filter #(not (fs/hidden? %)) (fs/list-dir dir))) 0))]
    (if non-empty-dir
      nil
      (do
        (when (fs/exists? dir)
          (delete-dir dir))
        (println "Clone " (s/replace url #"http://leejoker.top:11566/" "") " to " (str dir))
        (try (with-open [g (-> git
                               (.setURI url)
                               (.setDirectory (fs/file dir))
                               (.setCloneAllBranches false)
                               (.setDepth 1)
                               (.setProgressMonitor (TextProgressMonitor.))
                               (.call))]
               g)
             (catch Exception e
               (println "Retry " retry-times "times")
               (if (> retry-times 3)
                 (println "Clone " (s/replace url #"http://leejoker.top:11566/" "") " to " (str dir) " failed: " e)
                 (clone-repo url dir (inc retry-times)))))))))

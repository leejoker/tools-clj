(ns config.config
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [util.os :refer [env-path]]))

(defrecord TclConfig [home downloads plugins installs shims])

(defn tools-home
  []
  (let [home-dir (fs/path (env-path) ".tools-clj")
        inner-dirs '("downloads" "plugins" "installs" "shims")]
    (doseq [dir (map #(fs/path home-dir %) inner-dirs)]
      (when-not (fs/exists? dir)
        (fs/create-dirs dir)))
    (TclConfig. (str (fs/absolutize home-dir))
                (str (fs/absolutize (fs/path home-dir "downloads")))
                (str (fs/absolutize (fs/path home-dir "plugins")))
                (str (fs/absolutize (fs/path home-dir "installs")))
                (str (fs/absolutize (fs/path home-dir "shims"))))))
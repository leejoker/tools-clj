(ns config.config
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [util.os :refer [create-dirs env-path]]))

(defrecord TclConfig [home plugins shims])

(defn tools-home
  []
  (let [home-dir (env-path)
        inner-dirs '("plugins" "shims")]
    (doseq [dir (map #(fs/path home-dir %) inner-dirs)]
      (create-dirs dir))
    (TclConfig. (str (fs/absolutize home-dir))
                (str (fs/absolutize (fs/path home-dir "plugins")))
                (str (fs/absolutize (fs/path home-dir "shims"))))))

(defn create-plugin-dir
  [plugin-name]
  (let [config (tools-home)
        dir    (create-dirs (fs/path (:plugins config) plugin-name))]
    (str (fs/absolutize dir))))
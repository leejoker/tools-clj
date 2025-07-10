(ns plugins.scoop
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [config.config :refer [create-plugin-dir]]
   [util.git :refer [clone-repo]]
   [util.os :refer [create-dirs ps-version?]]))

(def ^:const SCOOP_PACKAGE_GIT_REPO "http://leejoker.top:11566/https://github.com/ScoopInstaller/Scoop.git")

(def ^:const BUCKETS
  {:main "http://leejoker.top:11566/https://github.com/ScoopInstaller/Main.git"
   :versions "http://leejoker.top:11566/https://github.com/ScoopInstaller/Versions.git"
   :extras "http://leejoker.top:11566/https://github.com/ScoopInstaller/Extras.git"
   :nerd-fonts "http://leejoker.top:11566/https://github.com/matthewjberger/scoop-nerd-fonts.git"})

(defn bucket-dirs
  [& dirs]
  (let [scoop-dir (create-plugin-dir "scoop")]
    (apply reduce (fn [m dir]
                    (let [dir-path (create-dirs (fs/path scoop-dir "buckets" dir))
                          bucket-key (keyword dir)]
                      (assoc m (str (fs/absolutize dir-path)) (bucket-key BUCKETS)))) {} dirs)))

(defn install
  []
  (if (ps-version?)
    (let [scoop-dir (create-plugin-dir "scoop")
          buckets '("main" "versions" "extras" "nerd-fonts")
          scoop-app-dir (create-dirs (fs/path scoop-dir "apps" "scoop" "current"))
          bucket-dir-map (bucket-dirs buckets)]
      (println "Scoop Installing...")
      (clone-repo SCOOP_PACKAGE_GIT_REPO scoop-app-dir 0)
      (println "Scoop Add Buckets...")
      (doseq [[k v] bucket-dir-map]
        (clone-repo v k 0)))
    ;; TODO shim , global env
    (println "Please upgrade your PowerShell version to 5.1 or higher.")))
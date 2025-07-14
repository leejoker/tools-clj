(ns plugins.scoop
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [config.config :refer [create-plugin-dir tools-home]]
   [util.git :refer [clone-repo]]
   [util.os :refer [create-dirs ps-version?]])
  (:import [scoop ScoopTemplate]))

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

(defn write-scoop-shims-file
  [scoop-app-dir]
  (let [config (tools-home)
        scoop-ps (str (fs/absolutize (fs/path scoop-app-dir "bin" "scoop.ps1"))) 
        ps1-file (ScoopTemplate/scoopPowershell scoop-ps)
        cmd-file (ScoopTemplate/scoopCmd scoop-ps)
        sh-file (ScoopTemplate/scoopShell scoop-ps)
        scoop-shims {(str (fs/absolutize (fs/path (:shims config) "scoop.ps1"))) ps1-file
                     (str (fs/absolutize (fs/path (:shims config) "scoop.cmd"))) cmd-file
                     (str (fs/absolutize (fs/path (:shims config) "scoop")))     sh-file}]
    (doseq [[k v] scoop-shims]
      (spit k v))))


(defn install-scoop
  []
  (if (ps-version?)
    (let [scoop-dir      (create-plugin-dir "scoop")
          buckets        '("main" "versions" "extras" "nerd-fonts")
          scoop-app-dir  (create-dirs (fs/path scoop-dir "apps" "scoop" "current"))
          bucket-dir-map (bucket-dirs buckets)]
      (println "Scoop Installing...")
      (clone-repo SCOOP_PACKAGE_GIT_REPO scoop-app-dir 0)
      (println "Scoop Add Buckets...")
      (doseq [[k v] bucket-dir-map]
        (clone-repo v k 0))
      (write-scoop-shims-file scoop-app-dir)
      (System/exit 0))
    (println "Please upgrade your PowerShell version to 5.1 or higher.")))
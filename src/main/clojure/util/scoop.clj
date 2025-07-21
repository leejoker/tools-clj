(ns util.scoop
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [config.config :refer [create-plugin-dir tools-home]]
   [util.git :refer [clone-repo]]
   [util.os :refer [create-dirs ps-version? string-format cmd-run add-path]]))

(def ^:const SCOOP_PACKAGE_GIT_REPO "http://leejoker.top:11566/https://github.com/ScoopInstaller/Scoop.git")

(def ^:const BUCKETS
  {:main "http://leejoker.top:11566/https://github.com/ScoopInstaller/Main.git"
   :versions "http://leejoker.top:11566/https://github.com/ScoopInstaller/Versions.git"
   :extras "http://leejoker.top:11566/https://github.com/ScoopInstaller/Extras.git"
   :nerd-fonts "http://leejoker.top:11566/https://github.com/matthewjberger/scoop-nerd-fonts.git"})

(def ^:const SCOOP_PS1_TEMPLATE
  (str "# {scoop}" (System/lineSeparator)
       "$path = Join-Path $PSScriptRoot \"..\\plugins\\scoop\\apps\\scoop\\current\\bin\\scoop.ps1\"" (System/lineSeparator)
       "if ($MyInvocation.ExpectingInput) { $input | & $path  @args } else { & $path  @args }" (System/lineSeparator)
       "exit $LASTEXITCODE"))

(def ^:const SCOOP_CMD_TEMPLATE
  (str "@rem {scoop}" (System/lineSeparator)
       "@echo off" (System/lineSeparator)
       "where /q pwsh.exe" (System/lineSeparator)
       "if %errorlevel% equ 0 (" (System/lineSeparator)
       "    pwsh -noprofile -ex unrestricted -file \"{scoop}\"  %*" (System/lineSeparator)
       ") else (" (System/lineSeparator)
       "    powershell -noprofile -ex unrestricted -file \"{scoop}\"  %*" (System/lineSeparator)
       ")"))

(def ^:const SCOOP_SH_TEMPLATE
  (str "#!/bin/sh\n"
       "# {scoop}\n"
       "if command -v pwsh.exe > /dev/null 2>&1; then\n"
       "    pwsh.exe -noprofile -ex unrestricted -file \"{scoop}\"  \"$@\"\n"
       "else\n"
       "    powershell.exe -noprofile -ex unrestricted -file \"{scoop}\"  \"$@\"\n"
       "fi"))

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
        ps1-file (string-format SCOOP_PS1_TEMPLATE (list scoop-ps))
        cmd-file (string-format SCOOP_CMD_TEMPLATE (repeat 3 scoop-ps))
        sh-file (string-format SCOOP_SH_TEMPLATE (repeat 3 scoop-ps))
        scoop-shims {(str (fs/absolutize (fs/path (:shims config) "scoop.ps1"))) ps1-file
                     (str (fs/absolutize (fs/path (:shims config) "scoop.cmd"))) cmd-file
                     (str (fs/absolutize (fs/path (:shims config) "scoop")))     sh-file}]
    (doseq [[k v] scoop-shims]
      (spit k v))))

(defn install-scoop
  []
  (if (ps-version?)
    (let [shims (:shims (tools-home))
          scoop-dir      (create-plugin-dir "scoop")
          buckets        '("main" "versions" "extras" "nerd-fonts")
          scoop-app-dir  (create-dirs (fs/path scoop-dir "apps" "scoop" "current"))
          bucket-dir-map (bucket-dirs buckets)]
      (println "Scoop Installing...")
      (clone-repo SCOOP_PACKAGE_GIT_REPO scoop-app-dir 0)
      (println "Scoop Add Buckets...")
      (doseq [[k v] bucket-dir-map]
        (clone-repo v k 0))
      (write-scoop-shims-file scoop-app-dir)
      (add-path shims)
      (println "Scoop Installed Successfully!")
      (System/exit 0))
    (println "Please upgrade your PowerShell version to 5.1 or higher.")))

(defn install-app
  [{:keys [options]}]
  (try
    (let [shims (:shims (tools-home))
          scoop-dir (create-plugin-dir "scoop")
          scoop-cmd (str (fs/absolutize (fs/path shims "scoop.cmd")))]
      (cmd-run (str "cmd.exe /c " scoop-cmd " install " options))
      (doseq [file (fs/list-dir (fs/path scoop-dir "shims"))]
        (fs/move file (fs/path shims (fs/file-name file)) {:replace-existing false})))
    (catch Exception _)))

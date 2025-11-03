(ns util.scoop
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [clojure.string :as s]
   [config.config :refer [create-plugin-dir tools-home]]
   [util.git :refer [clone-repo]]
   [util.global :refer [tryp]]
   [util.os :refer [add-path cmd-run create-dirs equal-ignore-case? load-config ps-version? remove-path string-format]]))

(def ^:const SCOOP_PACKAGE_GIT_REPO "http://leejoker.top:11566/https://github.com/ScoopInstaller/Scoop.git")

(def ^:const BUCKETS
  {:main          "http://leejoker.top:11566/https://github.com/ScoopInstaller/Main.git"
   :versions      "http://leejoker.top:11566/https://github.com/ScoopInstaller/Versions.git"
   :extras        "http://leejoker.top:11566/https://github.com/ScoopInstaller/Extras.git"
   :nerd-fonts    "http://leejoker.top:11566/https://github.com/matthewjberger/scoop-nerd-fonts.git"
   :scoop-clojure "http://leejoker.top:11566/https://github.com/littleli/scoop-clojure"})

(def ^:const SCOOP_PS1_TEMPLATE
  (str "# {scoop}" (System/lineSeparator)
       "$path = {scoop}" (System/lineSeparator)
       "if ($MyInvocation.ExpectingInput) { $input | & {scoop} @args } else { & {scoop} @args }" (System/lineSeparator)
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

(defn scoop-dir
  []
  (load-config :scoop (create-plugin-dir "scoop")))

(defn bucket-dirs
  [& dirs]
  (apply reduce (fn [m dir]
                  (let [dir-path (create-dirs (fs/path (scoop-dir) "buckets" dir))
                        bucket-key (keyword dir)]
                    (assoc m (str (fs/absolutize dir-path)) (bucket-key BUCKETS)))) {} dirs))

(defn write-scoop-shims-file
  [scoop-app-dir]
  (let [config (tools-home)
        scoop-ps (str (fs/absolutize (fs/path scoop-app-dir "bin" "scoop.ps1")))
        ps1-file (string-format SCOOP_PS1_TEMPLATE (repeat 4 scoop-ps))
        cmd-file (string-format SCOOP_CMD_TEMPLATE (repeat 3 scoop-ps))
        sh-file (string-format SCOOP_SH_TEMPLATE (repeat 3 scoop-ps))
        scoop-shims {(str (fs/absolutize (fs/path (:shims config) "scoop.ps1"))) ps1-file
                     (str (fs/absolutize (fs/path (:shims config) "scoop.cmd"))) cmd-file
                     (str (fs/absolutize (fs/path (:shims config) "scoop")))     sh-file}]
    (doseq [[k v] scoop-shims]
      (spit k v))))

(defn clean-shims
  []
  (tryp
   (let [shims (:shims (tools-home))
         files-in-shim (fs/list-dir shims)
         shim-files (filter #(= (fs/extension %) "shim") files-in-shim)]
     (doseq [f shim-files]
       (let [origin-file (-> (fs/absolutize f)
                             str
                             slurp
                             (s/split #"=")
                             second
                             (s/replace "\"" "")
                             (s/trim)
                             (s/trim-newline))]
         (when-not (fs/exists? origin-file)
           (fs/delete (first (filter #(equal-ignore-case? (fs/file-name %) (fs/file-name origin-file)) files-in-shim)))
           (fs/delete f)))))))

(defn remove-plugin-shims-env
  []
  (let [plugin-shims (str (fs/absolutize (fs/path (create-plugin-dir "scoop") "shims")))]
    (remove-path plugin-shims)))

(defn install-scoop
  [_]
  (if (ps-version?)
    (let [shims (:shims (tools-home))
          buckets '("main" "versions" "extras" "nerd-fonts" "scoop-clojure")
          scoop-app-dir (create-dirs (fs/path (scoop-dir) "apps" "scoop" "current"))
          bucket-dir-map (bucket-dirs buckets)]
      (println "Scoop Installing...")
      (clone-repo SCOOP_PACKAGE_GIT_REPO scoop-app-dir 0)
      (println "Scoop Add Buckets...")
      (doseq [[k v] bucket-dir-map]
        (clone-repo v k 0))
      (write-scoop-shims-file scoop-app-dir)
      (add-path shims)
      (println "Scoop Installed Successfully!"))
    (println "Please upgrade your PowerShell version to 5.1 or higher.")))

(defn list-app
  [_]
  (tryp
   (let [shims (:shims (tools-home))
         scoop-cmd (str (fs/absolutize (fs/path shims "scoop.cmd")))]
     (cmd-run (str "cmd.exe /c " scoop-cmd " list")))))

(defn install-app
  [{:keys [options]}]
  (tryp
   (let [shims (:shims (tools-home))
         scoop-cmd (str (fs/absolutize (fs/path shims "scoop.cmd")))]
     (cmd-run (str "cmd.exe /c " scoop-cmd " install " options))
     (doseq [file (fs/list-dir (fs/path (scoop-dir) "shims"))]
       (try
         (fs/move file (fs/path shims (fs/file-name file)) {:replace-existing false})
         (catch Exception _
           (fs/delete file)))))
   (remove-plugin-shims-env)))

(defn unregistry-shims
  [_]
  (let [shims (:shims (tools-home))]
    (remove-path shims)
    (remove-plugin-shims-env)
    (println "Unregistered Shims Successfully!")))

(defn uninstall-app
  [{:keys [options]}]
  (tryp
   (let [shims (:shims (tools-home))
         scoop-cmd (str (fs/absolutize (fs/path shims "scoop.cmd")))]
     (cmd-run (str "cmd.exe /c " scoop-cmd " uninstall " options))
     (clean-shims))
   (remove-plugin-shims-env)))

(defn update-app
  [{:keys [options]}]
  (tryp
   (let [shims (:shims (tools-home))
         scoop-cmd (str (fs/absolutize (fs/path shims "scoop.cmd")))]
     (cmd-run (str "cmd.exe /c " scoop-cmd " update"))
     (if (= "all" options)
       (cmd-run (str "cmd.exe /c " scoop-cmd " update *"))
       (when (not= true options)
         (cmd-run (str "cmd.exe /c " scoop-cmd " update " options)))))
   (remove-plugin-shims-env)))

(defn clean-scoop
  [_]
  (tryp
   (let [shims (:shims (tools-home))
         scoop-cmd (str (fs/absolutize (fs/path shims "scoop.cmd")))]
     (cmd-run (str "cmd.exe /c " scoop-cmd " cache rm * "))
     (cmd-run (str "cmd.exe /c " scoop-cmd " cleanup * "))
     (clean-shims))
   (remove-plugin-shims-env)))

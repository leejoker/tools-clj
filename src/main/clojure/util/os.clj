(ns util.os
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell process check]]
   [clojure.string :as s]))

(defn create-dirs
  [path]
  (when-not (fs/exists? path)
    (fs/create-dirs path))
  path)

(defn env-path
  []
  (let [tools-clj-home (System/getenv "TOOLS_CLJ_HOME")
        default-path   (fs/path (if (fs/windows?)
                                  (System/getenv "USERPROFILE")
                                  (System/getenv "HOME")) ".tools-clj")
        path (if (nil? tools-clj-home)
               default-path
               tools-clj-home)]
    (create-dirs (fs/path path))))

(defn cmd
  [command]
  (-> (shell {:out :string :inherit true} command)
      :out
      s/split-lines
      first))

(defn cmd-run
  [cmd]
  (let [proc (process {:cmd cmd :inherit true :encoding "UTF-8"})
        exit (:exit (check proc))]
    exit))

(defn ps-version?
  []
  (try
    (let [v (Double/parseDouble
             (cmd "powershell.exe -Command $PSVersionTable.PSVersion.ToString(2)"))]
      (>= v 5.1))
    (catch Exception e
      (println e)
      false)))

(defn delete-dir
  [dir-path]
  (if (fs/windows?)
    (cmd (str "cmd /c rmdir /S /Q " dir-path))
    (cmd (str "bash -c \"rm -rf " dir-path "\""))))

(defn string-format
  [format-string args]
  (reduce #(s/replace %1 "{scoop}" (str %2)) format-string args))

(defn get-system-env-var
  [var-name]
  (System/getenv var-name))

(defn set-system-env-var
  [var-name var-value]
  (let [var-value-set (into #{} (s/split var-value #";"))
        new-var-value (s/join ";" (map #(str %) var-value-set))
        cmd (str "reg add \"HKCU\\Environment\" /v " var-name " /t REG_SZ /d \"" new-var-value "\" /f")]
    (cmd-run (str "cmd /c " cmd))))

(defn add-path
  [new-path-value]
  (let [path (get-system-env-var "PATH")
        new-path (str path ";" new-path-value)]
    (set-system-env-var "PATH" new-path)))

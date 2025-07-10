(ns util.os
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell]]
   [clojure.string :as s]))

(defn env-path
  []
  (let [tools-clj-home (fs/path (System/getenv "TOOLS_CLJ_HOME"))
        default-path   (fs/path (if (fs/windows?)
                                  (System/getenv "USERPROFILE")
                                  (System/getenv "HOME")) ".tools-clj")]
    (if (nil? tools-clj-home)
      default-path
      tools-clj-home)))

(defn cmd
  [command]
  (-> (shell {:out :string :inherit true} command)
      :out
      s/split-lines
      first))

(defn ps-version?
  []
  (try
    (let [v (Double/parseDouble
             (cmd "powershell.exe -Command $PSVersionTable.PSVersion.ToString(2)"))]
      (>= v 5.1))
    (catch Exception e
      (println e)
      false)))

(defn create-dirs
  [path]
  (when-not (fs/exists? path)
    (fs/create-dirs path))
  path)

(defn delete-dir
  [dir-path]
  (if (fs/windows?)
    (cmd (str "cmd /c rmdir /S /Q " dir-path))
    (cmd (str "bash -c \"rm -rf " dir-path "\""))))
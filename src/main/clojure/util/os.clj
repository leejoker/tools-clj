(ns util.os
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [cheshire.core :as json]
   [babashka.process :refer [shell process check]]
   [clojure.string :as s])
  (:import [java.util Base64]
           [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn executable?
  [path]
  (if (fs/windows?)
    (or (s/ends-with? (s/lower-case (fs/absolutize path)) ".exe")
        (s/ends-with? (s/lower-case (fs/absolutize path)) ".bat")
        (s/ends-with? (s/lower-case (fs/absolutize path)) ".cmd")
        (s/ends-with? (s/lower-case (fs/absolutize path)) ".ps1"))
    (fs/executable? (fs/file path))))

(defn color-file
  [f value prefix suffix]
  (cond
    (fs/directory? (fs/file f)) (str (second prefix) value suffix)
    (executable? (fs/file f)) (str (first prefix) value suffix)
    :else value))

(defn date-formatter
  []
  (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))

(defn log-time
  []
  (let [now (LocalDateTime/now)]
    (.format now (date-formatter))))

(defn equal-ignore-case?
  [a b]
  (= (s/lower-case a) (s/lower-case b)))

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

(defn config-file
  []
  (fs/path (env-path) ".tclrc"))

(defn config-json
  []
  (let [cf (config-file)]
    (if (fs/exists? cf)
      (json/parse-string (slurp (str (fs/absolutize cf))) true)
      nil)))

(defn save-config-file
  [json]
  (let [f (config-file)]
    (spit (str (fs/absolutize f)) (json/encode json {:pretty true}) :append false)))

(defn load-config
  [key default-value]
  (let [json (config-json)
        v (if (nil? json) nil (key json))]
    (if (nil? v)
      default-value
      v)))

(defn cmd
  [command]
  (-> (shell {:out :string :inherit true} command)
      :out
      s/split-lines))

(defn cmd-run-quite
  [cmd]
  (let [proc (process {:cmd cmd :inherit false :encoding "UTF-8"})
        exit (:exit (check proc))]
    exit))

(defn cmd-run
  [cmd]
  (let [proc (process {:cmd cmd :inherit true :encoding "UTF-8"})
        exit (:exit (check proc))]
    exit))

(defn ps-version?
  []
  (try
    (let [v (Double/parseDouble
             (first (cmd "powershell.exe -Command $PSVersionTable.PSVersion.ToString(2)")))]
      (>= v 5.1))
    (catch Exception e
      (println e)
      false)))

(defn temp-dir
  []
  (if (fs/windows?)
    (System/getenv "TEMP")
    "/var/tmp"))

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
  (let [var-value-set (into (sorted-set) (s/split var-value #";"))
        new-var-value (s/join ";" (map #(str %) var-value-set))
        command (str "reg add \"HKCU\\Environment\" /v " var-name " /t REG_SZ /d \"" new-var-value "\" /f")]
    (cmd-run-quite (str "cmd /c " command))))

(defn add-path
  [new-path-value]
  (let [path (get-system-env-var "PATH")
        new-path (str path ";" new-path-value)]
    (set-system-env-var "PATH" new-path)
    (println "Update PATH Successfully")))

(defn remove-path
  [path-value]
  (let [path (get-system-env-var "PATH")
        path-set (into (sorted-set) (s/split path #";"))
        new-path (s/join ";" (remove #(s/includes? % path-value) path-set))]
    (set-system-env-var "PATH" new-path)))

(defn base64-encode
  [path]
  (let [bytes (fs/read-all-bytes (fs/absolutize path))
        base64 (.encodeToString (Base64/getEncoder) bytes)]
    base64))

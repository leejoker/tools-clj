(ns cmd.core
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [babashka.http-client :as http]
   [clojure.string :as s]
   [cmd.change-jetbrains-path :refer [run-cjp]]))

(defn fetch-remote-template
  [name]
  (let [http-base "https://gitee.com/monkeyNaive/tools-clj/raw/main/src/main/resources/"
        resp (http/get (str http-base name))]
    (get resp :body)))

(defn create-project-structure
  [path]
  (let [script-dir     (fs/path path "scripts")
        script-compile (fs/path script-dir "compile")
        script-clean   (fs/path script-dir "clean")
        src-dir        (fs/path path "src" "main" "clojure")
        src-resources  (fs/path path "src" "main" "resources")
        core           (fs/path src-dir "core.clj")
        readme         (fs/path path "README.md")
        ignore         (fs/path path ".gitignore")
        build          (fs/path path "build.clj")
        deps           (fs/path path "deps.edn")
        dirs           [script-dir src-dir src-resources]
        files          [script-compile script-clean core readme ignore build deps]]
    ;; 创建目录
    (doseq [dir dirs]
      (when-not (fs/exists? dir)
        (fs/create-dirs dir)))
    ;; 创建文件
    (doseq [file files]
      (when-not (fs/exists? file)
        (fs/create-file file)))))

(defn add-config
  [config-name path f opt]
  (let [template-name (str config-name ".tl")
        ^String content (fetch-remote-template template-name)
        ^String rewrite-content (if f (f content opt) nil)
        file-path (fs/path path config-name)]
    (if (nil? rewrite-content)
      (fs/write-bytes file-path (.getBytes (String. content)))
      (fs/write-bytes file-path (.getBytes (String. rewrite-content))))))

(defn overwrite-template-content
  "overwrite the template with args"
  [content kv-map]
  (reduce (fn [acc [k v]]
            (s/replace acc (str "${" (name k) "}") (str v)))
          content
          kv-map))

(defn ask-get-input
  [ask]
  (println ask)
  (read-line))

(defn read-user-input
  [project-name]
  (let [user-input-map {:groupId       (ask-get-input "Project groupId: ")
                        :artifactId    project-name
                        :version       "1.0.0-SNAPSHOT"
                        :jdkVersion    (ask-get-input "JDK Version(11 17 21): ")
                        :nameSpaces    "core"
                        :mainNameSpace "core"}]
    user-input-map))

(defn create-project
  [project-name]
  (let [path (fs/path "." project-name)
        opts (read-user-input project-name)]
    (create-project-structure path)
    (add-config "core.clj" (fs/path path "src" "main" "clojure") nil opts)
    (add-config "deps.edn" path nil opts)
    (add-config "build.clj" path overwrite-template-content opts)
    (add-config "compile" (fs/path path "scripts") overwrite-template-content opts)
    (add-config "clean" (fs/path path "scripts") overwrite-template-content opts)))

(defn print-help-message
  []
  (println "please use command: new cjp"))

(defn -main
  [& args]
  (let [command      (first args)
        project-name (second args)]
    (cond
      (nil? command) (print-help-message)
      (= command "new") (create-project project-name)
      (= command "cjp") (run-cjp)
      :else (print-help-message))))

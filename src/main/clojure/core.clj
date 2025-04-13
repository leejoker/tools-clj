(ns core
  (:gen-class)
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.string :as s]))


(defn create-project-structure
  [path]
  (let [src-dir (fs/path path "src" "main" "clojure")
        src-resources (fs/path path "src" "main" "resources")
        core (fs/path src-dir "core.clj")
        readme (fs/path path "README.md")
        ignore (fs/path path ".gitignore")
        build (fs/path path "build.clj")
        deps (fs/path path "deps.edn")
        dirs [src-dir src-resources]
        files [core readme ignore build deps]]
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
        content (slurp (io/resource template-name))
        rewrite-content (if f (f content opt) nil)
        file-path (fs/path path config-name)]
    (if (nil? rewrite-content)
      (fs/write-bytes file-path (.getBytes content))
      (fs/write-bytes file-path (.getBytes rewrite-content)))))

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
  []
  (let [user-input-map {:groupId       (ask-get-input "Project groupId: ")
                        :artifactId    (ask-get-input "Project artifactId: ")
                        :version       (ask-get-input "Project version: ")
                        :jdkVersion    (ask-get-input "JDK Version(11 17 21): ")
                        :nameSpaces    (ask-get-input "NS to Compile(separate with space): ")
                        :mainNameSpace (ask-get-input "Main NS: ")}]
    user-input-map))

(defn -main
  [& args]
  (let [path (first args)
        opts (read-user-input)]
    (create-project-structure path)
    (add-config "core.clj" (fs/path path "src" "main" "clojure") nil opts)
    (add-config "deps.edn" path nil opts)
    (add-config "build.clj" path overwrite-template-content opts)))

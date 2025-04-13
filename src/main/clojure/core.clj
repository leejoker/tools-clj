(ns core
  (:gen-class)
  (:require [babashka.fs :as fs]
            [clojure.string :as s]
            [clojure.java.io :as io]))

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

(defn add-build-clj
  [project-path options]
  (let [content (slurp (io/resource "build.clj.tl"))
        rewrite-content (overwrite-template-content content options)
        path (fs/path project-path)
        file-path (fs/path path "build.clj")]
    (if (or (not (fs/exists? path)) (not (fs/exists? file-path)))
      (try
        (if (not (fs/exists? path)) (fs/create-dirs path) true)
        (if (not (fs/exists? file-path)) (fs/create-file file-path) true)
        (catch Exception e
          (println "error: " (.getMessage e))))
      nil)
    (fs/write-bytes file-path (.getBytes rewrite-content))))

;; TODO create project dir, create deps.edn
(defn -main
  [& args]
  (add-build-clj (first args) (read-user-input)))

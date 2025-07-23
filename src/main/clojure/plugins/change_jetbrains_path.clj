(ns plugins.change-jetbrains-path
  (:gen-class)
  (:require
   [clojure.string :as str]
   [babashka.fs :as fs]
   [util.global :refer [tryp]]))

(defrecord Config [jetbrains-apps jetbrains-config])

(defn find-properties-files
  [^Config config]
  (let [dirs (fs/list-dir (:jetbrains-apps config))]
    (map #(fs/path % "bin" "idea.properties") dirs)))

(defn replace-lines
  [config]
  {"# idea.config.path" #(str/replace % "# idea.config.path=${user.home}" (str "idea.config.path=" (:jetbrains-config config)))
   "# idea.system.path" #(str/replace % "# idea.system.path=${user.home}" (str "idea.system.path=" (:jetbrains-config config)))
   "# idea.plugins.path" #(str/replace % "# idea.plugins.path" "idea.plugins.path")
   "# idea.log.path" #(str/replace % "# idea.log.path" "idea.log.path")})

(defn modify-properties
  [^Config config]
  (let [properties-files (find-properties-files config)
        replacements (replace-lines config)]
    (doseq [f properties-files]
      (println (str "Check File " f))
      (let [content                  (fs/read-all-lines f)
            [new-content modify-num] (reduce
                                      (fn [[result modify-num] line]
                                        (if-let [replace-fn (some
                                                             #(when (str/starts-with? line %) (replacements %))
                                                             (keys replacements))]
                                          [(conj result (replace-fn line)) (inc modify-num)]
                                          [(conj result line) modify-num]))
                                      [[] 0]
                                      content)]
        (if (zero? modify-num)
          (println "Nothing to change")
          (do
            (println (str "Update Config: " f))
            (spit (fs/file f) (str/join \newline new-content))))
        (println "==========================================================")))))

(defn run-cjp
  [_]
  (tryp
   (let [jetbrains-home (System/getenv "JETBRAINS_HOME")]
     (if (nil? jetbrains-home)
       (println "JETBRAINS_HOME not found")
       (do
         (println (str "JetBrains Home: " jetbrains-home))
         (println "==========================================================")
         (modify-properties (Config. (str jetbrains-home "/apps")
                                     (str jetbrains-home "/tool-data"))))))))

(ns console.eol
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [clojure.string :as s]))

(defrecord ZeolConfig [path type extension hidden])

(defn change-eol
  [content type]
  (cond
    (= type "LF") (s/replace content #"\r\n|\r|\n" "\n")
    (= type "CRLF") (s/replace content #"\r\n|\r|\n" "\r\n")
    :else content))

(defn same-extension?
  [file extension]
  (if (nil? extension)
    true
    (= extension (fs/extension file))))

(defn handle-file
  [config]
  (let [path (:path config)
        type (:type config)
        extension (:extension config)
        hidden (:hidden config)
        abs-path (str (fs/absolutize path))]
    (when (and (same-extension? path extension) (if hidden true (not (fs/hidden? path))))
      (println "Processing file:" abs-path)
      (let [content (slurp abs-path)]
        (spit abs-path (change-eol content type))))))

(defn run-eol
  [{:keys [opts args]}]
  (let [paths (if (empty? args) [] args)]
    (doseq [path paths]
      (fs/walk-file-tree path {:visit-file (fn [f _]
                                             (handle-file (ZeolConfig. f (:type opts) (:extension opts) (:hidden opts)))
                                             :continue)
                               :pre-visit-dir (fn [d _] (println "Visiting directory:" (str d)) :continue)
                               :post-visit-dir (fn [d _] (println "Finished visiting directory:" (str d)) :continue)
                               :visit-file-failed (fn [f ex] (println "Failed to process file:" (str f) ", failed reason:" ex))}))))

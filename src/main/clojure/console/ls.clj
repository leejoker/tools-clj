(ns console.ls
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [cheshire.core :as json]
   [util.draw-table :as dt :refer [column-sub column-width]]
   [util.os :refer [env-path]])
  (:import
   (java.nio.file.attribute FileTime)
   (java.time ZoneId)
   (java.time.format DateTimeFormatter)))

(def date-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))

(defn sort-file-infos
  [file-infos index compare-func reverse-file-list]
  (let [comp (if (nil? compare-func) compare compare-func)
        sorted (sort-by #(nth % index) comp file-infos)]
    (if reverse-file-list
      (reverse sorted)
      sorted)))

(def options
  {"-fa" '(0 nil false)
   "-fd" '(0 nil true)
   "-tf" '(1 #(if (= % "File") 1 0) false)
   "-td" '(1 #(if (= % "Directory") 1 0) false)
   "-sa" '(2 nil false)
   "-sd" '(2 nil true)
   "-ca" '(3 nil false)
   "-cd" '(3 nil true)
   "-ma" '(4 nil false)
   "-md" '(4 nil true)})

(defn without-hidden-files
  [file-infos]
  (filter #(not (nth % 5)) file-infos))

(defn default-column-width
  [default-value]
  (let [config-file (fs/path (env-path) ".tclrc")]
    (if (fs/exists? config-file)
      (int (:filenameMaxWidth (json/parse-string (slurp (str (fs/absolutize config-file))) true)))
      default-value)))

(defn handle-col-width
  [column]
  (let [col-len (column-width column)
        dcw     (default-column-width 70)]
    (if (> col-len dcw)
      (str (column-sub column dcw 0 "") "...")
      column)))

(defn handle-file-sort
  [file-infos params]
  (let [without-hidden-file (empty? (apply filter #(= % "-a") params))
        files               (if without-hidden-file (without-hidden-files file-infos) file-infos)]
    (if (empty? params)
      files
      (apply reduce (fn [f param]
                      (let [opt (get options param)]
                        (if (nil? opt)
                          f
                          (sort-file-infos f (first opt) (second opt) (last opt))))) files params))))

(defn format-datetime
  [^FileTime file-time]
  (let [local-date-time (-> file-time
                            (.toInstant)
                            (.atZone (ZoneId/systemDefault))
                            (.toLocalDateTime))]
    (.format local-date-time date-formatter)))

(defn ls-header
  []
  (seq '("File Name(f)" "File Type(t)" "File Size(s)" "Create Time(c)" "Modified Time(m)")))

(defn readable-len
  [length]
  (let [byte-len length
        kb (quot byte-len 1024)
        mb (quot kb 1024)
        gb (quot mb 1024)]
    (if (= byte-len 0)
      "0"
      (if (= kb 0)
        (str byte-len " Byte")
        (if (= mb 0)
          (str kb " KB")
          (if (= gb 0)
            (str mb " MB")
            (str gb " GB")))))))

(defn file-info
  [f]
  (let [name          (handle-col-width (fs/file-name f))
        file-type     (if (fs/directory? f) "Directory" "File")
        len           (if (= file-type "Directory") " " (readable-len (fs/size f)))
        create-time   (format-datetime (fs/creation-time (fs/path f)))
        modified-time (format-datetime (fs/last-modified-time (fs/path f)))
        hidden        (fs/hidden? f)]
    (seq (list name file-type len create-time modified-time hidden))))

(defn list-files
  [path & args]
  (let [p          (fs/absolutize path)
        files      (fs/list-dir p)
        file-infos (handle-file-sort (map #(file-info %) files) args)
        table      (dt/create-table (ls-header) (map #(take 5 %) file-infos))]
    (dt/draw-table table)))

(defn list-current-path-files
  [& args]
  (list-files "." args))
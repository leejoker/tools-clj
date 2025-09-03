(ns console.ls
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [util.draw-table :as dt :refer [column-sub column-width]]
   [util.os :refer [load-config date-formatter]])
  (:import
   (java.nio.file.attribute FileTime)
   (java.time ZoneId)))

(defn sort-file-infos
  [file-infos index reverse-file-list]
  (let [sorted (sort-by #(nth % index) file-infos)]
    (if reverse-file-list
      (reverse sorted)
      sorted)))

(def options
  {:filename-asc '(0 false)
   :filename-desc '(0 true)
   :type-file '(1 true)
   :type-directory '(1 false)
   :size-asc '(6 false)
   :size-desc '(6 true)
   :create-asc '(3 false)
   :create-desc '(3 true)
   :modify-asc '(4 false)
   :modify-desc '(4 true)})

(defn without-hidden-files
  [file-infos]
  (filter #(not (nth % 5)) file-infos))

(defn default-column-width
  [default-value]
  (load-config :filenameMaxWidth default-value))

(defn handle-col-width
  [column]
  (let [col-len (column-width column)
        dcw     (default-column-width 70)]
    (if (> col-len dcw)
      (str (column-sub column dcw 0 "") "...")
      column)))

(defn handle-file-sort
  [file-infos opts]
  (let [without-hidden-file (not (:all opts))
        files               (if without-hidden-file (without-hidden-files file-infos) file-infos)]
    (if (empty? opts)
      files
      (reduce (fn [f key]
                (let [opt (key options)]
                  (if (nil? opt)
                    f
                    (sort-file-infos f (first opt) (last opt))))) files (keys opts)))))

(defn format-datetime
  [^FileTime file-time]
  (let [local-date-time (-> file-time
                            (.toInstant)
                            (.atZone (ZoneId/systemDefault))
                            (.toLocalDateTime))]
    (.format local-date-time (date-formatter))))

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
        len           (if (= file-type "Directory") 0 (fs/size f))
        rlen          (if (= file-type "Directory") " " (readable-len (fs/size f)))
        create-time   (format-datetime (fs/creation-time (fs/path f)))
        modified-time (format-datetime (fs/last-modified-time (fs/path f)))
        hidden        (fs/hidden? f)]
    (seq (list name file-type rlen create-time modified-time hidden len))))

(defn list-files
  [path opts]
  (let [p          (fs/absolutize path)
        files      (fs/list-dir p)
        file-infos (handle-file-sort (map #(file-info %) files) opts)
        table      (dt/create-table (ls-header) (map #(take 5 %) file-infos))]
    (dt/draw-table table)))

(defn list-current-path-files
  [{:keys [opts]}]
  (list-files "." opts))

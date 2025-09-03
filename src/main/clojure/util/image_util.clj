(ns util.image-util
  (:gen-class)
  (:require [babashka.fs :refer [read-all-bytes]]
            [util.log :refer [debug]]
            [clojure.string :as s]))

(defn bytes->hex
  [^bytes bs]
  (s/join " " (map #(str "0x" (format "%02X" %)) bs)))

(defn png-header-valid?
  [buffer]
  (let [header (bytes->hex (take 8 buffer))
        expected "0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A"]
    (debug (str "Header: " header))
    (= header expected)))

(defn png-iend-valid?
  [buffer]
  (let [iend (bytes->hex (take-last 12 buffer))
        expected "0x00 0x00 0x00 0x00 0x49 0x45 0x4E 0x44 0xAE 0x42 0x60 0x82"]
    (debug (str "IEnd: " iend))
    (= iend expected)))

(defn png-valid? [path]
  (let [buffer (read-all-bytes path)]
    (and (png-header-valid? buffer)
         (png-iend-valid? buffer))))

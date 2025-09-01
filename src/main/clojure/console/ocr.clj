(ns console.ocr
  (:gen-class)
  (:require [util.os :refer [load-config cmd-run cmd]]
            [babashka.fs :as fs]
            [clojure.string :as s]))

(defrecord OcrConfig [snip-exec-path tesseract-path tmp-image-path])

(defn load-orc-config
  []
  (let [snip-exec-path (load-config :snipExecPath "snipaste")
        tesseract-path (load-config :tesseractPath "tesseract")
        tmp-image-path (load-config :tmpImagePath (System/getenv "TEMP"))]
    (OcrConfig. snip-exec-path tesseract-path tmp-image-path)))

(defn run-ocr
  [_]
  (let [config (load-orc-config)
        image-path (str (fs/absolutize (fs/path (:tmp-image-path config) "ocr.png")))
        capture-cmd (str "cmd.exe /c " (:snip-exec-path config) " snip -o " image-path)]
    (when (fs/exists? image-path)
      (fs/delete image-path))
    (cmd-run capture-cmd)
    (loop [image image-path]
      (if (fs/exists? image)
        (let [result-lines (cmd (str "cmd.exe /c " (:tesseract-path config) " " image-path " stdout -l eng+chi_sim"))
              result-str (s/join (System/lineSeparator) result-lines)]
          (cmd-run (str "powershell -NoProfile -Command \"Set-Clipboard -Value '" result-str "'\""))
          (System/exit 0))
        (recur image)))))

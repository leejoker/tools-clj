(ns console.ocr
  (:gen-class)
  (:require [util.os :refer [load-config cmd-run cmd base64-encode]]
            [babashka.fs :as fs]
            [clojure.string :as s]
            [util.ollama :as ollama]))

(defrecord OcrConfig [snip-exec-path tesseract-path tmp-image-path base-url model-name])

(defn load-orc-config
  []
  (let [snip-exec-path (load-config :snipExecPath "snipaste")
        tesseract-path (load-config :tesseractPath "tesseract")
        tmp-image-path (load-config :tmpImagePath (System/getenv "TEMP"))
        base-url (load-config :baseUrl "http://127.0.0.1:11434")
        model-name (load-config :modelName "qwen2.5vl:3b")]
    (OcrConfig. snip-exec-path tesseract-path tmp-image-path base-url model-name)))

(defn local-ocr
  [tesseract-path image-path]
  (let [result-lines (cmd (str "cmd.exe /c " tesseract-path " " image-path " stdout -l eng+chi_sim"))
        result-str (s/join (System/lineSeparator) result-lines)]
    (cmd-run (str "powershell -NoProfile -Command \"Set-Clipboard -Value '" result-str "'\""))
    (System/exit 0)))

(defn ollama-ocr
  [image-path base-url model-name]
  (ollama/generate base-url
                   model-name
                   "帮我识别图片中的内容并以纯文本的格式输出，不要包含其他的文字，也不要输出markdown格式的数据"
                   (conj [] (base64-encode image-path))))

(defn run-ocr
  [{:keys [opts]}]
  (let [config (load-orc-config)
        image-path (str (fs/absolutize (fs/path (:tmp-image-path config) "ocr.png")))
        capture-cmd (str "cmd.exe /c " (:snip-exec-path config) " snip -o " image-path)]
    (when (fs/exists? image-path)
      (fs/delete image-path))
    (cmd-run capture-cmd)
    (loop [image image-path]
      (if (fs/exists? image)
        (if (= (:type opts) "local")
          (local-ocr (:tesseractPath config) image-path)
          (ollama-ocr image-path (:base-url config) (:model-name config)))
        (recur image)))))

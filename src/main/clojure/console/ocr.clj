(ns console.ocr
  (:gen-class)
  (:require
   [babashka.fs :as fs]
   [clojure.string :as s]
   [util.image-util :refer [png-valid?]]
   [util.ollama :as ollama]
   [util.os :refer [base64-encode cmd cmd-run load-config temp-dir]])
  (:import (jna TclLib)))

(defrecord OcrConfig [snip-exec-path tesseract-path tmp-image-path base-url model-name])

(defn load-orc-config
  []
  (let [snip-exec-path (load-config :snipExecPath "snipaste")
        tesseract-path (load-config :tesseractPath "tesseract")
        tmp-image-path (load-config :tmpImagePath (temp-dir))
        base-url (load-config :baseUrl "http://127.0.0.1:11434")
        model-name (load-config :modelName "qwen2.5vl:3b")]
    (OcrConfig. snip-exec-path tesseract-path tmp-image-path base-url model-name)))

(defn local-ocr
  [tesseract-path image-path]
  (let [result-lines (cmd (str tesseract-path " " image-path " stdout -l eng+chi_sim"))
        result-str (s/join System/lineSeparator result-lines)]
    result-str))

(defn ollama-ocr
  [image-path base-url model-name]
  (let [f (if (= (load-config :modelType "ollama") "ollama") ollama/generate ollama/openai-generate)]
    (f base-url
       model-name
       "帮我识别图片中的内容，并输出原始内容，不要包含其他的文字，也不要输出markdown格式的数据"
       (conj [] (base64-encode image-path)))))

(defn handle-ocr-content
  [content]
  (s/trim-newline (s/replace (s/replace-first content #"```(\w+)\n" "") #"```" "")))

(defn copy-to-clipboard
  [content]
  (.SetClipboardUTF8 TclLib/INSTANCE (handle-ocr-content content)))

(defn run-ocr
  [{:keys [opts]}]
  (let [config (load-orc-config)
        image-path (str (fs/absolutize (fs/path (:tmp-image-path config) "ocr.png")))
        capture-cmd (str (:snip-exec-path config) " snip -o " image-path)]
    (when (fs/exists? image-path)
      (fs/delete image-path))
    (cmd-run capture-cmd)
    (loop [image image-path]
      (if (and (fs/exists? image) (png-valid? image))
        (let [content (if (= (:type opts) "local")
                        (local-ocr (:tesseractPath config) image-path)
                        (ollama-ocr image-path (:base-url config) (:model-name config)))]
          (copy-to-clipboard content))
        (recur image)))))

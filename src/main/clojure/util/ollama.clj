(ns util.ollama
  (:gen-class)
  (:require [babashka.http-client :refer [post]]
            [cheshire.core :as json]))

(defn generate
  [base-url model-name prompt images]
  (let [url (str base-url "/api/generate")
        body (let [default-body {:model model-name
                                 :prompt prompt
                                 :stream false}]
               (if (empty? images)
                 default-body
                 (assoc default-body :images images)))
        resp (post url {:body (json/encode body)})]
    (:response (json/decode (:body resp) true))))

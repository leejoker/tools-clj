(ns util.ollama
  (:gen-class)
  (:require [babashka.http-client :refer [post]]
            [cheshire.core :as json]
            [util.global :refer [debug]]))

(defn generate
  [base-url model-name prompt images]
  (let [url (str base-url "/api/generate")
        body (let [default-body {:model model-name
                                 :prompt prompt
                                 :stream false}]
               (if (empty? images)
                 default-body
                 (assoc default-body :images images)))
        resp (post url {:body (json/encode body)})
        resp-body (json/decode (:body resp) true)]
    (debug resp-body)
    (:response resp-body)))

(ns util.ollama
  (:gen-class)
  (:require [babashka.http-client :refer [post]]
            [cheshire.core :as json]
            [util.log :refer [debug]]
            [util.os :refer [load-config]]))

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
    (debug (load-config :debug false) resp-body)
    (:response resp-body)))

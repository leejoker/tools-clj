(ns util.ollama
  (:gen-class)
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]
            [util.log :refer [debug]]
            [util.os :refer [load-config]]))

(defn get-token
  []
  (let [url (load-config :tokenUrl "")
        app-key (load-config :appKey "")
        app-secret (load-config :appSecret "")
        resp-body (json/decode
                    (:body (http/get url {:query-params {"appKey" app-key "secretKey" app-secret}}))
                    true)]
    (:accessToken (:data resp-body))))

(defn openai-message
  [prompt images]
  (let [prompt-message {:type "text" :text prompt}
        content (if (empty? images)
                  [prompt-message]
                  [prompt-message {:type "image_url" :image_url {:url (str "data:image/png;base64," (first images))}}])]
    [{:role "user" :content content}]))

(defn openai-generate
  [base-url model-name prompt images]
  (let [url (str base-url "/v1/chat/completions")
        body {:model    model-name
              :stream   false
              :messages (openai-message prompt images)}
        resp (http/post url {:body    (json/encode body)
                             :headers {"Content-Type"              "application/json"
                                       "access_token"              (get-token)
                                       (load-config :apiHeader "") (load-config :apiCode "")}})
        resp-body (json/decode (:body resp) true)]
    (debug resp-body)
    (:content (:message (first (:choices resp-body))))))

(defn generate
  [base-url model-name prompt images]
  (let [url (str base-url "/api/generate")
        body (let [default-body {:model  model-name
                                 :prompt prompt
                                 :stream false}]
               (if (empty? images)
                 default-body
                 (assoc default-body :images images)))
        resp (http/post url {:body (json/encode body)})
        resp-body (json/decode (:body resp) true)]
    (debug resp-body)
    (:response resp-body)))

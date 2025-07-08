(ns util.os
  (:gen-class)
  (:require
   [babashka.fs :refer [windows?]]
   [babashka.http-client :as http]
   [cheshire.core :as json]))

(defn env-path
  []
  (if (windows?)
    (System/getenv "USERPROFILE")
    (System/getenv "HOME")))

(defn get-public-ip []
  (try
    (let [response (http/get "https://api.ipify.cn?format=json")
          body (json/parse-string (:body response) true)]
      (:ip body))
    (catch Exception e
      (println "Error fetching public IP:" (.getMessage e))
      nil)))
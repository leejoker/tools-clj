(ns http-demo
  (:require [babashka.http-client :as http]
            [clojure.test :refer [with-test]]))

(with-test
  (defn http-get-test
    [url]
    (http/get url))
  (let [resp (http-get-test "https://gitee.com/monkeyNaive/tools-clj/raw/main/src/main/resources/build.clj.tl")]
    (println (:body resp))))
(ns mouse-server.json-utils.json
  (:require [cheshire.core :refer :all]))

(defn to-str [mp]
 (generate-string mp))

(defn to-mp [json-str]
  (parse-string json-str))

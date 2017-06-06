(ns avalon.build
  (:require [clojure.data.json :as json]))

(defn version [v]
  (spit "resources/version.json" (json/write-str {"version" v})))

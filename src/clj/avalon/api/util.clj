(ns avalon.api.util
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

;; convert the body to a reader. Useful for testing in the repl
;; where setting the body to a string is much simpler.
(defn body-as-string [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      java.lang.String body
      (slurp (io/reader body)))))

;; For PUT and POST parse the body as json and store in the context
;; under the given key.
(defn parse-json [ctx key]
  (when (#{:put :post} (get-in ctx [:request :request-method]))
    (try
      (if-let [body (body-as-string ctx)]
        (let [data (json/read-str body :key-fn keyword)]
          [false {key data}])
        {:message "No body"})
      (catch Exception e
        (.printStackTrace e)
        {:message (format "IOException: %s" (.getMessage e))}))))

(defn malformed? [data-key]
  #(parse-json % data-key))

(defn require-fields [fields data-key]
  #(if (#{:post} (get-in % [:request :request-method]))
    (every? (data-key %) fields)
    true))

(defn update-fields [fields data-key]
  #(if (#{:put} (get-in % [:request :request-method]))
    (every? (partial contains? fields) (keys (data-key %)))
    true))

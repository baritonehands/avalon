(ns avalon.models.people
  (:require [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]))

(defonce people (db/create-db))

(defrecord Person [name])

(defn create-person [name]
  (let [person (->Person name)]
    (crud/create people person)))

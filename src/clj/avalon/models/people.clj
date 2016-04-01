(ns avalon.models.people
  (:require [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]))

(defonce people (db/create-db))

(defrecord Person [name])

(defn create! [name]
  (let [person (->Person name)]
    (crud/create! people person)))

(defn named [m name]
  (let [people (->> (:people m)
                    (map (partial crud/get people))
                    (filter #(= 0 (.compareToIgnoreCase name (:name %)))))]
    (map :id people)))

(defn delete! [db id name]
  (crud/update! db id
                (fn [game]
                  (let [to-delete (named game name)]
                    (map (partial crud/delete! people) to-delete)
                    (update game :people #(apply disj % to-delete))))))

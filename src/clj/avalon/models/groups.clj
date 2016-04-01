(ns avalon.models.groups
  (:require [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]))

(defonce groups (db/create-db))

(defrecord Group [name code people])

(defn named [name]
  (let [group (->> (crud/all groups)
                    (filter #(= 0 (.compareToIgnoreCase name (:name %)))))]
    (seq (map :id group))))

(defn create! [name code]
  (let [group (->Group name code #{})]
    (crud/create! groups group)))

(defn display [group]
    (-> group
        (dissoc :code)
        (update :people (partial map #(:name (crud/get people/people %))))))

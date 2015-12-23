(ns avalon.models.groups
  (:require [avalon.models.people :as people]))

(defonce groups (ref {}))

(defrecord Group [id name code people])

(defn create-group [name code]
  (let [group (->Group (str (java.util.UUID/randomUUID)) name code #{})]
    (dosync (alter groups assoc (:id group) group))
    group))

(defn get-group [id]
  (@groups id))

(defn add-person [id person]
  (dosync (alter groups update-in [id :people] conj (:id person))))

(defn display-group [group]
    (-> group
        (dissoc :code)
        (assoc :people (map people/get-person (:people group)))))

(defn display-all []
  (vec (for [[_ v] @groups] (display-group v))))

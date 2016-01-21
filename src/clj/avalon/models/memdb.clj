(ns avalon.models.memdb
  (:require [avalon.models.crud :refer :all]))

(defn create-db []
  (let [db (ref {})]
    (reify CRUD
      (create [_ entity]
        (let [id (str (java.util.UUID/randomUUID))
              entity (assoc entity :id id)]
          (dosync (alter db assoc id entity))
            entity))
      (all [_]
        (vals @db))
      (get [_ id]
        (@db id))
      (exists? [_ id]
        (contains? @db id))
      (save [_ id updates]
        (dosync (alter db update-in [id] merge updates)))
      (relate [_ id k v]
        (dosync (alter db update-in [id k] conj v)))
      (delete [_ id]
        (dosync (dissoc db id))))))

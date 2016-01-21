(ns avalon.models.crud)

(defprotocol CRUD
  (create [this entity])
  (get [this id])
  (all [this])
  (exists? [this id])
  (save [this id updates])
  (relate [this id k v])
  (delete [this id]))

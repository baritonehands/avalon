(ns avalon.models.crud)

(defprotocol CRUD
  (create! [this entity])
  (get [this id])
  (all [this])
  (exists? [this id])
  (update! [this id f])
  (save! [this id updates])
  (relate! [this id k v])
  (delete! [this id]))

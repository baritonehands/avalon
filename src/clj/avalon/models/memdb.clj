(ns avalon.models.memdb
  (:use avalon.models.crud))

(defn- char-range [c n]
  (let [idx (int c)]
    (map char (range idx (+ idx n)))))

(def alphabet
  (let [numbers (char-range \0 10)]
    (->> (char-range \a 26)
         (remove #{\a \e \i \o \u})
         (concat numbers))))

(defn- id-gen []
  (apply str (repeatedly 6 #(rand-nth alphabet))))

(defn create-db []
  (let [db (ref {})]
    (reify CRUD
      (create! [_ entity]
        (let [id (str (id-gen))
              entity (assoc entity :id id)]
          (dosync (alter db assoc id entity))
            entity))
      (all [_]
        (vals @db))
      (get [_ id]
        (@db id))
      (exists? [_ id]
        (contains? @db id))
      (update! [_ id f]
        (dosync (alter db update id f)))
      (save! [_ id updates]
        (dosync (alter db update-in [id] merge updates)))
      (relate! [_ id k v]
        (dosync (alter db update-in [id k] conj v)))
      (delete! [_ id]
        (dosync (dissoc db id))))))

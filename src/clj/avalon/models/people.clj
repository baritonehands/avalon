(ns avalon.models.people)

(defonce people (ref {}))

(defrecord Person [id name])

(defn create-person [name]
  (let [person (->Person (str (java.util.UUID/randomUUID)) name)]
    (dosync (alter people assoc (:id person) person))
    person))

(defn get-person [id]
  (@people id))

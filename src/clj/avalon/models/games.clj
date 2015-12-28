(ns avalon.models.games
  (:use [clojure.set])
  (:require [avalon.models.people :as people]))

(defonce games (ref {}))

(defrecord Game [id group-id roles status people])

(defn create-game [group-id]
  (let [game (->Game
                 (str (java.util.UUID/randomUUID))
                 group-id
                 #{:merlin :percival :mordred :morgana}
                 :waiting
                 #{})]
    (dosync (alter games assoc (:id game) game))
      game))

(defn exists? [id]
  (contains? @games id))

(defn get-game [id]
  (@games id))

(defn update-game [id updates]
  (dosync (alter games update-in [id] merge updates)))

(defn display-game [game]
  (-> (into {} (seq game))
      (rename-keys {:group-id :groupId})
      (assoc :people (map people/get-person (:people game)))))

(defn add-person [id person]
  (dosync (alter games update-in [id :people] conj (:id person))))

(ns avalon.models.games
  (:use [clojure.set])
  (:require [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]))

(defonce games (db/create-db))

(defrecord Game [group-id roles status people])

(defn create-game [group-id]
  (let [game (->Game
                 group-id
                 #{:merlin :percival :mordred :morgana}
                 :waiting
                 #{})]
    (crud/create! games game)))

(defn display-game [game]
  (-> (into {} (seq game))
      (rename-keys {:group-id :groupId})
      (assoc :people (map (partial crud/get people/people) (:people game)))))

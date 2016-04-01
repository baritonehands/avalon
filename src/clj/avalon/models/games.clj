(ns avalon.models.games
  (:use [clojure.set])
  (:require [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]))

(defonce games (db/create-db))

(defrecord Game [group-id roles status people teams])

(defn create! [group-id]
  (let [game (->Game
                 group-id
                 #{:merlin :percival :mordred :morgana}
                 :waiting
                 #{}
                 {})]
    (crud/create! games game)))

(defn display [game]
  (-> (dissoc game :teams)
      (dissoc :first)
      (rename-keys {:group-id :groupId})
      (update :people (partial map #(:name (crud/get people/people %))))))


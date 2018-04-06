(ns avalon.models.games
  (:use [clojure.set])
  (:require [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]))

(defonce games (db/create-db))

(defrecord Game [group-id roles status people teams])

(defn create-game [group-id]
  (let [game (->Game
                 group-id
                 #{:merlin :percival :mordred :morgana}
                 :waiting
                 #{}
                 {})]
    (crud/create! games game)))

(defn display-game [game]
  (-> (dissoc game :teams)
      (dissoc :first)
      (rename-keys {:group-id :groupId})
      (update :people (partial map #(:name (crud/get people/people %))))))

(defn add-person [id person]
  (crud/relate! games id :people (:id person)))

(defn people-named [game name]
  (let [people (->> (:people game)
                    (map (partial crud/get people/people))
                    (filter #(= 0 (.compareToIgnoreCase name (:name %)))))]
    (map :id people)))

(defn delete-person [id name]
  (crud/update! games id
                (fn [game]
                  (let [people (people-named game name)]
                    (map (partial crud/delete! people/people) people)
                    (update game :people #(apply disj % people))))))

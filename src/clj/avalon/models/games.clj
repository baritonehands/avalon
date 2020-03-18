(ns avalon.models.games
  (:use [clojure.set])
  (:require [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]
            [clojure.string :as s]))

(defonce games (db/create-db))

(defrecord Game [roles status people teams])

(defn create-game []
  (let [game (->Game
                 #{:merlin :percival :mordred :morgana}
                 :waiting
                 #{}
                 {})]
    (crud/create! games game)))

(defn display-game [game]
  (-> (dissoc game :teams)
      (dissoc :first)
      (update :people (partial map #(:name (crud/get people/people %))))))

(defn add-person [id person]
  (crud/relate! games id :people (:id person)))

(defn people-named [game name]
  (let [people (->> (:people game)
                    (map (partial crud/get people/people))
                    (filter #(= 0 (.compareToIgnoreCase (s/trim name) (:name %)))))]
    (map :id people)))

(defn delete-person [id name]
  (crud/update! games id
                (fn [game]
                  (let [people (people-named game name)]
                    (map (partial crud/delete! people/people) people)
                    (update game :people #(apply disj % people))))))

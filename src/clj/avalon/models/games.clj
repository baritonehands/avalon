(ns avalon.models.games
  (:use [clojure.set])
  (:require [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.models.memdb :as db]
            [clojure.string :as s]))

(defonce games (db/create-db))

(defrecord Game [roles status people teams quests vote])

(defn create-game []
  (let [game (->Game
               #{:merlin :percival :mordred :morgana}
               :waiting
               #{}
               {}
               []
               nil)]
    (crud/create! games game)))

(defn people->names [{:keys [people] :as game}]
  (let [pair (juxt identity #(:name (crud/get people/people %)))
        id->name (into {} (map pair people))
        names #(mapv id->name %)]
    (-> game
        (update :people names)
        (update :seats names)
        (update :quests #(mapv names %)))))

(defn display-game [game]
  (-> (dissoc game :teams :first :vote)
      (people->names)))

(defn add-person [id person]
  (crud/relate! games id :people (:id person)))

(defn people-named [game names]
  (let [name-set (->> names
                      (map (comp s/lower-case s/trim))
                      set)
        people (->> (:people game)
                    (map (partial crud/get people/people))
                    (filter #(contains? name-set (s/lower-case (:name %)))))]
    (set (map :id people))))

(defn delete-person [id pname]
  (dosync
    (let [people (people-named (crud/get games id) [pname])]
      (crud/update! games id
                    (fn [game]
                      (update game :people #(apply disj % people))))
      (doseq [person people]
        (crud/delete! people/people person)))))

(defn add-vote [id vote names]
  (crud/update! games id
                (fn [game]
                  (let [people (people-named game names)]
                    (assoc game :vote {:type   vote
                                       :people people
                                       :votes  {}})))))

(defn vote-complete? [{:keys [people votes]}]
  (= people (set (keys votes))))

(defn results [{:keys [people votes]}]
  (let [results (frequencies (vals votes))]
    (assoc results :people people)))

(defn update-vote [id person-id choice]
  (crud/update! games id
                (fn [game]
                  (let [next-game (assoc-in game [:vote :votes person-id] choice)
                        next-vote (:vote next-game)]
                    (if (vote-complete? next-vote)
                      (-> next-game
                          (assoc :vote nil)
                          (update :quests conj (results next-vote)))
                      next-game)))))

(defn cancel-vote [id]
  (crud/update! games id
                (fn [game]
                  (assoc game :vote nil))))

(defn clear-quest [id n]
  (crud/update! games id
                (fn [game]
                  (update game :quests #(vec (take (dec n) %))))))

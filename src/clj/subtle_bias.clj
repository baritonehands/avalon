(ns subtle-bias
  (:require [avalon.models.games :as games]
            [avalon.models.people :as people]
            [avalon.models.crud :as crud]
            [avalon.api.games :refer [start-game]])
  (:gen-class :main true))

(defn good? [v]
  (if (#{:good :merlin :percival} v)
    true
    false))

(defn randomness-proof [n]
  (let [dist (atom {})]
    (dotimes [_ n]
      (let [game (games/create-game nil)]
        (dotimes [i 10]
          (let [person (people/create-person (str "Player " i))]
            (games/add-person (:id game) person)))
        (crud/update! games/games (:id game) start-game)
        (let [teams (vec (vals (:teams (crud/get games/games (:id game)))))
              res [(good? (teams 0)) (good? (teams 7))]]
          (doseq [person (:people (crud/get games/games (:id game)))]
            (crud/delete! people/people person))
          (crud/delete! games/games (:id game))
          (swap! dist update res #(if % (inc %) 1)))))
    @dist))

(defn -main [& args]
  (-> (randomness-proof (Integer/valueOf (or (nth args 0) "1000")))
      (clojure.pprint/pprint)))

(ns user
  (:require [avalon.models.groups :as groups]
            [avalon.models.people :as people]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]
            [clojure.pprint :refer [pprint]]))

(defn fill-test-data [n]
  (let [;group (groups/create-group "Testing" "123")
        game (games/create-game nil)]
    (dotimes [player n]
      (let [person (people/create-person (str "Player" player))]
        (games/add-person (:id game) person)))
    ;(println "Group" (:id group))
    (println "Game" (:id game))
    (println (str "http://localhost:3449/games/" (:id game)))))

(defn play-roles [id]
  (pprint (into {}
        (for [[person-id role] (:teams (crud/get games/games id))]
          {role (str "http://localhost:3449/games/" id "/play/" person-id)}))))

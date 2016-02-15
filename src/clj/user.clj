(ns user
  (:require [avalon.models.groups :as groups]
            [avalon.models.people :as people]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]))

(defn fill-test-data [n]
  (let [group (groups/create-group "Testing" "123")
        game (games/create-game (:id group))]
    (dotimes [player n]
      (let [person (people/create-person (str "Player" player))]
        (games/add-person (:id game) person)))
    (println "Group" (:id group))
    (println "Game" (:id game))))

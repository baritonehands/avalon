(ns user
  (:require [avalon.models.groups :as groups]
            [avalon.models.people :as people]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]
            [clojure.pprint :refer [pprint]]))

(defn- fill-test-data [db create-fn args n]
  (let [m (apply create-fn args)]
    (dotimes [player n]
      (let [person (people/create! (str "Player" player))]
        (crud/relate! db (:id m) :people (:id person))))
    m))

(defn fill-test-game
  ([n]
   (fill-test-game n nil))
  ([n group-id]
   (println "Game" (:id (fill-test-data games/games games/create! [group-id] n)))))

(defn fill-test-group [name n]
  (if-not (groups/named name)
    (do (println "Group"
                 (:id (fill-test-data groups/groups groups/create! [name "123"] n)))
        (println "Code 123"))
    (println "Group already exists by that name")))

(defn play-roles [id]
  (pprint
    (for [[person-id role] (:teams (crud/get games/games id))]
      [role (str "http://localhost:3449/games/" id "/play/" person-id)])))

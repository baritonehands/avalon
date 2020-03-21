(ns user
  (:require [avalon.models.people :as people]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]
            [clojure.pprint :refer [pprint]]
            [clojure.java.browse :refer [browse-url]]))

(defn fill-test-data [n]
  (let [;group (groups/create-group "Testing" "123")
        game (games/create-game)]
    (dotimes [player n]
      (let [person (people/create-person (str "Player " (inc player)))]
        (games/add-person (:id game) person)))
    ;(println "Group" (:id group))
    (println "Game" (:id game))))

(defn fill-votes
  ([id] (fill-votes id 0))
  ([id fails]
   (let [{:keys [vote]} (crud/get games/games id)
         people (shuffle (:people vote))
         choices (concat (repeat fails :failure) (repeat :success))]
     (doseq [[person-id choice] (map vector people choices)]
       (games/update-vote id person-id choice)))))

(defn play-roles [id]
  (pprint
    (for [[person-id role] (:teams (crud/get games/games id))]
      [role (str "http://localhost:9500/games/" id "/play/" person-id)])))

(defn browse-roles [id]
  (doseq [[person-id _] (:teams (crud/get games/games id))]
    (browse-url (str "http://localhost:9500/games/" id "/play/" person-id))))

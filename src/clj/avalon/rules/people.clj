(ns avalon.rules.people
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            [avalon.models.crud :as crud]
            [avalon.models.games :as games]))

(defn person-rules [method game]
  (let [taken? (fn [pname]
                 (if (= method :post)
                   (= (count (games/people-named game [pname])) 0)
                   true))]
    {:id [[(fn [_] (some? game)) :message "Game with that code does not exist"]]
     :name [v/required
            [taken? :message "There is already a player with that name"]]}))


(def add-person-rules
  {:status [[#{:waiting} :message "Game already started"]]})

(defn valid-person? [id kw key]
  (fn [ctx]
    (let [game (crud/get games/games id)
          rules (person-rules (get-in ctx [:request :request-method]) game)
          valid (and (b/valid? (kw ctx) rules)
                     (b/valid? game add-person-rules))
          errors (concat (first (b/validate (kw ctx) rules))
                         (first (b/validate game add-person-rules)))]
      [valid {key errors}])))

(def info-rules
  {:status [[#{:playing} :message "Game not started"]]})

(defn valid-info? [game]
  (let [valid (b/valid? game info-rules)
        errors (first (b/validate game info-rules))]
    [valid errors]))

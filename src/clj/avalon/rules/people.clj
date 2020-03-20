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
    {:id   [[(fn [_] (some? game)) :message "Game with that code does not exist"]]
     :name [v/required
            [taken? :message "There is already a player with that name"]]}))


(def add-person-rules
  {:status [[#{:waiting} :message "Game already started"]]})

(defn valid-person? [id data-key error-key]
  (fn [ctx]
    (let [game (crud/get games/games id)
          rules (person-rules (get-in ctx [:request :request-method]) game)
          valid (and (b/valid? (data-key ctx) rules)
                     (b/valid? game add-person-rules))
          errors (concat (first (b/validate (data-key ctx) rules))
                         (first (b/validate game add-person-rules)))]
      [valid {error-key errors}])))

(def info-rules
  {:status [[#{:playing} :message "Game not started"]]})

(defn valid-info? [game]
  (let [valid (b/valid? game info-rules)
        errors (first (b/validate game info-rules))]
    [valid errors]))

(defn person-voted? [person-id]
  (fn [{:keys [votes]}]
    (not-any? #(= % person-id) (keys votes))))

(defn vote-rules [person-id]
  {:vote [[#(contains? (:people %) person-id) :message "You cannot vote"]
          [(person-voted? person-id) :message "You already voted"]]
   :choice [[#{"success" "failure"} :message "Choice must be success or failure"]]})

(defn valid-vote? [id person-id data error-key]
  (let [{:keys [vote]} (crud/get games/games id)
        to-validate (merge data {:vote vote})
        rules (vote-rules person-id)
        valid (b/valid? to-validate rules)
        errors (first (b/validate to-validate rules))]
    [valid {error-key errors}]))

(ns avalon.rules.games
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]))

(def play-game-rules
  {:people [[v/min-count 5]
            [v/max-count 10]]
   :status [[#{:waiting} :message "Game already started"]]})

(defn- create-validator [rules]
  (fn [id key]
    (let [game (crud/get games/games id)
          valid (b/valid? game rules)
          errors (first (b/validate game rules))]
      [valid {key errors}])))

(def valid-play? (create-validator play-game-rules))

(def update-roles-rules
  {:name   [[v/member #{"merlin" "morgana" "percival" "mordred" "oberon" "assassin"}]]
   :status [[#{:waiting} :message "Roles cannot be updated after game is started"]]})

(defn valid-role? [id name key]
  (let [game (crud/get games/games id)
        to-validate {:name name :status (.status game)}
        valid (b/valid? to-validate update-roles-rules)
        errors (first (b/validate to-validate update-roles-rules))]
    [valid {key errors}]))

(def good {:specials #{:merlin :percival}
           :counts   [3 4 4 5 6 6]})

(def bad {:specials #{:mordred :morgana :oberon :assassin}
          :counts   [2 2 3 3 3 4]})

(defn assign-team [m team roles n]
  (let [special (filter (:specials m) roles)
        unnamed (repeat (- ((:counts m) (- n 5)) (count special)) team)]
    (concat special unnamed)))

(defn assign-roles [game]
  (let [people (.people game)
        roles (.roles game)
        blue (assign-team good :good roles (count people))
        red (assign-team bad :bad roles (count people))]
    (zipmap people (shuffle (concat red blue)))))

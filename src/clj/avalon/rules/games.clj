(ns avalon.rules.games
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]))
(def good {:specials #{:merlin :percival :good-lancelot}
           :counts   [3 4 4 5 6 6]})

(def bad {:specials #{:mordred :morgana :oberon :evil-lancelot}
          :counts   [2 2 3 3 3 4]})

(defn valid-specials? [game]
  (fn [roles]
    (let [specials (:specials bad)
          counts (:counts bad)
          num-players (count (:people game))
          in-play (count (filter specials roles))]
      (and (> num-players 4)
           (>= (counts (- num-players 5)) in-play)))))

(defn play-game-rules [game]
  {:people [[v/min-count 5]
            [v/max-count 10]]
   :status [[#{:waiting} :message "Game already started"]]
   :roles  [[(valid-specials? game) :message "Too many evil roles"]]})

(defn- create-validator [rules-fn]
  (fn [id key]
    (let [game (crud/get games/games id)
          rules (rules-fn game)
          valid (b/valid? game rules)
          errors (first (b/validate game rules))]
      [valid {key errors}])))

(def valid-play? (create-validator play-game-rules))

(def update-roles-rules
  {:name   [[v/member #{"merlin" "morgana" "percival" "mordred" "oberon" "lancelot"}]]
   :status [[#{:waiting} :message "Roles cannot be updated after game is started"]]})

(defn valid-role? [id name key]
  (let [game (crud/get games/games id)
        to-validate {:name name :status (.status game)}
        valid (b/valid? to-validate update-roles-rules)
        errors (first (b/validate to-validate update-roles-rules))]
    [valid {key errors}]))

(defn assign-team [m team roles n]
  (let [special (filter (:specials m) roles)
        size ((:counts m) (- n 5))
        use-assassin (and (= team :bad)
                          (roles :merlin)
                          (> size (count special)))
        special (if use-assassin (conj special :assassin) special)
        unnamed (repeat (- size (count special)) team)]
    (concat special unnamed)))

(defn assign-roles [game]
  (let [people (.people game)
        roles (.roles game)
        roles (if (roles :lancelot)
                (conj roles :good-lancelot :evil-lancelot)
                roles)
        blue (assign-team good :good roles (count people))
        red (assign-team bad :bad roles (count people))]
    (zipmap people (shuffle (concat red blue)))))

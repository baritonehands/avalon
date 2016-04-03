(ns avalon.rules.games
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            [avalon.models.games :as games]
            [avalon.models.crud :as crud]))
(def good {:specials #{:merlin :percival :good-lancelot :twin1 :twin2}
           :counts   [3 4 4 5 6 6]})

(def bad {:specials #{:mordred :morgana :oberon :evil-lancelot}
          :counts   [2 2 3 3 3 4]})

(defn- add-doubles [single one two]
  (fn [roles]
    (if (roles single)
      (disj (conj roles one two) single)
      roles)))

(def add-lancelot (add-doubles :lancelot :good-lancelot :evil-lancelot))
(def add-twins (add-doubles :twins :twin1 :twin2))

(defn valid-specials? [game team]
  (fn [roles]
    (let [roles (add-twins (add-lancelot roles))
          specials (:specials team)
          counts (:counts team)
          num-players (count (:people game))
          in-play (count (filter specials roles))]
      (if (and (>= num-players 5) (<= num-players 10))
        (>= (get counts (- num-players 5)) in-play)))))

(defn play-game-rules [game]
  {:people [[v/min-count 5 :message "Too few players"]
            [v/max-count 10 :message "Too many players"]]
   :status [[#{:waiting} :message "Game already started"]]
   :roles  [[(valid-specials? game bad) :message "Too many evil roles"]
            [(valid-specials? game good) :message "Too many good roles"]]})

(defn- create-validator [rules-fn]
  (fn [id key]
    (let [game (crud/get games/games id)
          rules (rules-fn game)
          valid (b/valid? game rules)
          errors (first (b/validate game rules))]
      [valid {key errors}])))

(def valid-play? (create-validator play-game-rules))

(def update-roles-rules
  {:name   [[v/member #{"merlin" "morgana" "percival" "mordred" "oberon" "lancelot" "twins"}]]
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
        roles (add-twins (add-lancelot (.roles game)))
        blue (assign-team good :good roles (count people))
        red (assign-team bad :bad roles (count people))]
    (zipmap people (shuffle (concat red blue)))))

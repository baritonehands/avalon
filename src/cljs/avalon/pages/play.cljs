(ns avalon.pages.play
  (:require [ajax.core :refer [GET]]
            [reagent.session :as session]
            [avalon.utils :refer [row col capitalize]]
            [avalon.pages.games :as games]
            [material-ui.core :as ui :include-macros true]
            [accountant.core :as route]))

(defn description [role]
  (let [name (capitalize (name role))]
    (cond
      (= role "good") [:h4 "You are a loyal servant of Arthur."]
      (= role "merlin") [:h4 "You are the great wizard " [:strong name] "."]
      (= role "percival") [:h4 "You are " [:strong name]]
      (= role "mordred") [:h4 "You are the dark lord " [:strong name] "."]
      (= role "morgana") [:h4 "You are the evil witch " [:strong name] "."]
      (= role "oberon") [:h4 "You are the evil force " [:strong name] "."]
      (= role "bad") [:h4 "You are a minion of Mordred."]
      (= role "assassin") [:h4 "You are the assassin and a minion of Mordred."]
      :else [:h3 "Your role is " [:strong name]])
    ))

(defn view_list [role]
    (cond
      (= role "merlin") [:h4 "The following are the minions of Modred:"]
      (= role "percival") [:h4 "Merlin is one of the following:"]
      (= role "mordred") [:h4 "The following are your minions:"]
      ; (= role "morgana") [:h4 "The following are the other minions of Modred:"]
      (some #{role} '("morgana" "assassin" "bad")) [:h4 "The following are Modred and his other minions:"]
      :else [:h3 "Your role is " [:strong name]])
    )

(defn info-view []
  (let [info (session/get :info)]
    (if info
      [:div
       [row
        [col
         [description (:role info)]]]
       [row
        [col
         [:div
          (if (> (count (:info info)) 0)
            [view_list (:role info)])
          (for [player (:info info)]
            [:div.player player])]]]
          [row
            [col
              [:h5 [:strong (:first info)] " is the first player. The player to his/her right is the Lady of the Lake."]]]
       [row
        [col
         [row
          [:div.col-xs-8.col-xs-offset-2.start-btn
           [ui/RaisedButton {:label      "Leave Game"
                             :fullWidth  true
                             :onTouchTap #(route/navigate! "/")}]]]]]]
      [:h3.text-center "Loading..."])))

(defn play-page []
  (let [game (session/get :game)]
    (if (= (:status game) "playing")
      [info-view]
      [games/game-page])))

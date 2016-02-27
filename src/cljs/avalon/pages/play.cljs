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
      (= role "assassin") [:h4 "You are the " [:strong name] " and a minion of Mordred."]
      (= role "evil-lancelot") [:h4 "You are " [:strong "Evil Lancelot"] " and a minion of Mordred."]
      (= role "good-lancelot") [:h4 "You are " [:strong "Good Lancelot"] ", a servant of Arthur."]
      :else [:h4 "Your role is " [:strong name]])
    ))

(defn view-list [info]
  (let [role (:role info)]
    [:div
     [:div
      (cond
        (= role "merlin") [:h5 "The following are the minions of Mordred:"]
        (= role "percival") [:h5 "Merlin is one of the following:"]
        (= role "mordred") [:h5 "The following are your minions:"]
        (= role "good-lancelot") [:h5 "The following is Evil Lancelot:"]
        (#{"morgana" "assassin" "bad" "evil-lancelot"} role) [:h5 "The following are Mordred and his other minions:"])
      (for [player (first (:info info))]
        [:div.player player])]
     (if (= role "evil-lancelot")
       [:div {:style {:padding-top "10px"}}
        [:h5 "The following is Good Lancelot:"]
        [:div.player (first (second (:info info)))]])]))

(defn info-view []
  (let [info (session/get :info)]
    (if info
      [:div
       [row
        [col
         [description (:role info)]]]
       [row
        [col
         [:div {:style {:padding-bottom "20px"}}
          [view-list info]]]]
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
      [row [col [:div.text-center [ui/CircularProgress]]]])))

(defn play-page []
  (let [game (session/get :game)]
    (if (= (:status game) "playing")
      [info-view]
      [games/game-page])))

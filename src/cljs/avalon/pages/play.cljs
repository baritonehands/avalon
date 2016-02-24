(ns avalon.pages.play
  (:require [ajax.core :refer [GET]]
            [reagent.session :as session]
            [avalon.utils :refer [row col capitalize]]
            [avalon.pages.games :as games]
            [material-ui.core :as ui :include-macros true]
            [accountant.core :as route]))

(defn description [role]
  (let [name (capitalize (name role))]
    [:h3 "Your role is " [:strong name]]))

(defn info-view []
  (let [info (session/get :info)]
    (if info
      [:div
       [row
        [col
         [description (:role info)]]]
       (if (:first info)
         [row [col [:h5 "You are the " [:strong "first player"] ". The player to your right is the Lady of the Lake."]]])
       [row
        [col
         [:div
          (if (> (count (:info info)) 0)
            [:h4 "You see:"])
          (for [player (:info info)]
            [:div.player player])]]]
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

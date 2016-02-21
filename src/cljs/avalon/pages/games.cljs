(ns avalon.pages.games
  (:require [ajax.core :refer [GET POST DELETE]]
            [cljs.pprint :refer [pprint]]
            [reagent.session :as session]
            [avalon.utils :refer [row col]]
            [material-ui.core :as ui :include-macros true]))

(defn get-game! [id]
  (let [game (session/get :game)]
    (when (or (nil? game) (not= id (:id game)))
      (GET (str "/api/games/" id) {:response-format :json
                                   :keywords?       true
                                   :handler         #(session/put! :game %)}))))

(defn toggle-role [id role on]
  (let [verb (if on POST DELETE)]
    (verb (str "/api/games/" id "/roles/" (.toLowerCase role))
          {:response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)})))

(defn role-toggle [id roles role]
  (let [on ((set roles) (.toLowerCase role))]
    [row
     [col
      [ui/Toggle {:defaultToggled on
                  :onToggle       #(toggle-role id role (not on))} role]]]))

(defn game-page []
  (let [game (session/get :game)
        {:keys [id people roles]} game]
    (if game
      [:div
       [row
        [col
         [:h2.status "Waiting for players..."]
         [:h3.code "Access code: " id]]]
       [row
        [col
         (for [player people]
           [:div.player player])]
        [:div.col-sm-8 [:pre (with-out-str (pprint game))]]]
       (for [role ["Merlin" "Percival" "Mordred" "Morgana" "Oberon"]]
         [role-toggle id roles role])
       [row
        [col
         [ui/RaisedButton {:primary  true
                           :label    "Start"
                           :on-click #(js/alert "Hello!")}]]]]
      ["Loading..."])))

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

(defn delete-player [id name]
  (DELETE (str "/api/games/" id "/people")
          {:params          {:name name}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)}))

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

(defn start-game [id]
  (POST (str "/api/games/" id "/play")
        {:response-format :json
         :keywords?       true
         :handler         #(session/put! :game %)}))

(defn game-page []
  (let [game (session/get :game)
        {:keys [id people roles]} game]
    (if game
      [:div
       [row
        [col
         [:div.text-center
          [:h3.status "Waiting for players..."]
          [:h4.code "Access code: " id]]]]
       [row
        [col
         (for [player people]
           [:div.player
            [ui/IconButton {:iconClassName "mdfi_action_delete"
                            :mobile?       true
                            :on-click      #(delete-player id player)}]
            player])]
        [:div.col-sm-8 [:pre (with-out-str (pprint game))]]]
       (for [role ["Merlin" "Percival" "Mordred" "Morgana" "Oberon"]]
         [role-toggle id roles role])
       [row
        [col
         [ui/RaisedButton {:primary  true
                           :label    "Start"
                           :on-click #(start-game id)}]]]]
      ["Loading..."])))

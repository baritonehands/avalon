(ns avalon.pages.games
  (:require [ajax.core :refer [GET POST DELETE]]
            [cljs.pprint :refer [pprint]]
            [reagent.session :as session]
            [avalon.utils :refer [row col]]
            [material-ui.core :as ui :include-macros true]
            [accountant.core :as route]
            [reagent.core :as r]))

(defn get-game! [id & {:keys [force] :or {force false}}]
  (let [game (session/get :game)]
    (when (or force (not= id (:id game)))
      (GET (str "/api/games/" id) {:response-format :json
                                   :keywords?       true
                                   :handler         #(session/put! :game %)}))))

(defn get-info! [id person-id]
  (GET (str "/api/games/" id "/people/" person-id "/info")
       {:response-format :json
        :keywords?       true
        :handler         #(session/put! :info %)}))

(defn delete-player! [id name]
  (DELETE (str "/api/games/" id "/people")
          {:params          {:name name}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)}))

(defn toggle-role! [id role on]
  (let [verb (if on POST DELETE)]
    (verb (str "/api/games/" id "/roles/" (.toLowerCase role))
          {:response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)})))

(defn role-toggle [id roles role]
  (let [on (string? ((set roles) (.toLowerCase role)))]
    [row
     [col
      [ui/Toggle {:defaultToggled on
                  :onToggle       #(toggle-role! id role (not on))} role]]]))

(defn start-game! [id]
  (POST (str "/api/games/" id "/play")
        {:response-format :json
         :keywords?       true
         :handler         (fn [resp]
                            (session/put! :game resp)
                            (route/navigate! (str "/games/" id "/play/" (session/get :person-id))))}))

(defn refresh-game []
  (let [params (session/get :route-params)]
    (get-game! (:id params) :force true)
    (get-info! (:id params) (:person-id params))))

(def game-page
  (let [timer (r/atom nil)]
    (with-meta
      (fn []
        (let [game (session/get :game)
              {:keys [id people roles]} game]
          (if game
            [:div
             [row
              [col
               [:div.text-center
                [:h3.status "Waiting for players..."]
                [:h4.code "Access code: " [:pre id]]]]]
             [row
              [col
               (for [player people]
                 [:div.player
                  [ui/IconButton {:iconClassName "mdfi_action_delete"
                                  :touch         true
                                  :onTouchTap    #(delete-player! id player)}]
                  player])]]
             (for [role ["Merlin" "Percival" "Mordred" "Morgana" "Oberon"]]
               ^{:key role} [role-toggle id roles role])
             [row
              [col
               [ui/RaisedButton {:primary    true
                                 :label      "Start"
                                 :onTouchTap #(start-game! id)}]]]]
            [:h3.text-center "Loading..."])))
      {:component-did-mount #(reset! timer (js/setInterval refresh-game 5000))
       :component-will-unmount #(js/clearInterval @timer)})))

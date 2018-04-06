(ns avalon.pages.games
  (:require [ajax.core :refer [GET POST DELETE]]
            [cljs.pprint :refer [pprint]]
            [reagent.session :as session]
            [avalon.utils :refer [row col capitalize show-error]]
            [material-ui.core :as ui :include-macros true]
            [accountant.core :as route]
            [reagent.core :as r]))

(defn handle-start-error [{:keys [status response]}]
  (condp = status
    422 (show-error "Unable to Start Game" (capitalize (first (val (first response)))))
    status (show-error "Unable to Start Game" "Unexpected error, please try again")))

(defn handle-delete-error [{:keys [status response]}]
  (condp = status
    422 (show-error "Unable to Delete Player" (-> response first second first))
    status (show-error "Unable to Start Game" "Unexpected error, please try again")))

(defn handle-info-error [{:keys [status response]}]
  (condp = status
    404 (route/navigate! "/")
    nil))

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
        :handler         #(session/put! :info %)
        :error-handler   handle-info-error}))

(defn delete-player! [id name]
  (DELETE (str "/api/games/" id "/people")
          {:params          {:name name}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)
           :error-handler   handle-delete-error}))

(defn toggle-role! [id role on]
  (let [verb (if on POST DELETE)]
    (verb (str "/api/games/" id "/roles/" (.toLowerCase role))
          {:response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)})))

(defn role-toggle [id roles role desc]
  (let [on (string? ((set roles) role))]
    [ui/Toggle {:label         desc
                :labelPosition "left"
                :toggled       on
                :labelStyle    {:font-weight "normal"}
                :onToggle      #(toggle-role! id role (not on))}]))

(defn start-game! [id]
  (POST (str "/api/games/" id "/play")
        {:response-format :json
         :keywords?       true
         :handler         #(session/put! :game %)
         :error-handler   handle-start-error}))

(defn refresh-game []
  (let [params (session/get :route-params)]
    (get-game! (:id params) :force true)
    (get-info! (:id params) (:person-id params))))

(def role-options [["merlin" "Merlin"]
                   ["percival" "Percival"]
                   ["mordred" "Mordred"]
                   ["morgana" "Morgana"]
                   ["oberon" "Oberon"]
                   ["lancelot1" "Lancelot (known to each other)"]
                   ["lancelot2" "Lancelot (switch allegiance)"]
                   ["twins" "Twins"]])

(defn game-page []
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
         (into [ui/List {:subheader ["Players - " (count people)]}]
               (for [player people]
                 [ui/ListItem
                  [:div.player
                   [ui/IconButton {:iconClassName "mdfi_action_delete"
                                   :onTouchTap    #(delete-player! id player)}]
                   player]]))
         [ui/ListDivider]
         [ui/List {:subheader "Roles"}
          (for [[role desc] role-options]
            ^{:key role}
            [ui/ListItem [role-toggle id roles role desc]])]
         [row
          [:div.col-xs-8.col-xs-offset-2.start-btn {:style {:margin-bottom "40px"}}
           [ui/RaisedButton {:primary    true
                             :label      "Start"
                             :fullWidth  true
                             :onTouchTap #(start-game! id)}]]]]]]
      [row [col [:div.text-center [ui/CircularProgress]]]])))

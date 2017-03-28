(ns avalon.pages.games
  (:require [ajax.core :refer [GET POST DELETE]]
            [cljs.pprint :refer [pprint]]
            [reagent.session :as session]
            [avalon.utils :refer [row col capitalize show-error]]
            [material-ui.core :as ui :include-macros true]
            [accountant.core :as route]
            [reagent.core :as r]))

(defn handle-error [{:keys [status response]}]
  (condp = status
    422 (show-error "Unable to Start Game" (capitalize (first (val (first response)))))
    status (show-error "Unable to Start Game" "Unexpected error, please try again")))

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
           :handler         #(session/put! :game %)
           :error-handler   handle-error}))

(defn toggle-role! [id role on]
  (let [verb (if on POST DELETE)]
    (verb (str "/api/games/" id "/roles/" (.toLowerCase role))
          {:response-format :json
           :keywords?       true
           :handler         #(session/put! :game %)})))

(defn role-toggle [id roles role]
  (let [on (string? ((set roles) (.toLowerCase role)))]
    [ui/Toggle {:label         role
                :labelPosition "left"
                :toggled       on
                :labelStyle    {:font-weight "normal"}
                :onToggle      #(toggle-role! id role (not on))}]))

(defn start-game! [id]
  (POST (str "/api/games/" id "/play")
        {:response-format :json
         :keywords?       true
         :handler         (fn [resp]
                            (session/put! :game resp)
                            (route/navigate! (str "/games/" id "/play/" (session/get :person-id))))
         :error-handler   handle-error}))

(defn refresh-game []
  (let [params (session/get :route-params)]
    (get-game! (:id params) :force true)
    (get-info! (:id params) (:person-id params))))

(def game-page
  (let [timer (r/atom nil)]
    (with-meta
      (fn []
        (let [game (session/get :game)
              {:keys [id people roles]} game
              error (session/get :error)]
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
                (for [role ["Merlin" "Percival" "Mordred" "Morgana" "Oberon" "Lancelot" "Twins"]]
                  ^{:key role}
                  [ui/ListItem [role-toggle id roles role]])]
               [row
                [:div.col-xs-8.col-xs-offset-2.start-btn {:style {:margin-bottom "40px"}}
                 [ui/RaisedButton {:primary    true
                                   :label      "Start"
                                   :fullWidth  true
                                   :onTouchTap #(start-game! id)}]]]]]]
            [row [col [:div.text-center [ui/CircularProgress]]]])))
      {:component-did-mount    #(reset! timer (js/setInterval refresh-game 5000))
       :component-will-unmount #(js/clearInterval @timer)})))

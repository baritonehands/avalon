(ns avalon.pages.games
  (:require [ajax.core :refer [GET POST DELETE]]
            [cljs.pprint :refer [pprint]]
            [reagent.session :as session]
            [avalon.utils :refer [row col spinner subheader-element form-control-label-full capitalize show-error make-styles]]
            [material-ui :as ui]
            [material-ui-icons :as icons]
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
    [:> form-control-label-full
     {:label           desc
      :label-placement "start"
      :control         (r/as-element
                         [:> ui/Switch {:checked   on
                                        :color     "primary"
                                        :on-change #(toggle-role! id role (not on))}])}]))

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
                   ["morgana" "Morgana"]
                   ["mordred" "Mordred"]
                   ["oberon" "Oberon"]
                   ["lancelot1" "Lancelot (known to each other)"]
                   ["lancelot2" "Lancelot (switch allegiance)"]
                   ["twins" "Twins"]])

(def use-styles
  (make-styles
    (fn [^Theme theme]
      {:container {:margin-top    (.spacing theme 2)
                   :margin-bottom (.spacing theme 4)}
       :pre       {:background-color "#f5f5f5"
                   :border           "1px solid #CCCCCC"
                   :border-radius    "5px"
                   :text-align       "center"
                   :width            "100%"}})))

(defn container [js-props]
  (let [classes (use-styles)]
    (r/as-element
      (into
        [:> ui/Grid {:container true
                     :justify   "center"
                     :spacing   2
                     :class     (:container classes)}]
        (.-children js-props)))))

(defn pre [js-props]
  (let [classes (use-styles)
        props (js->clj js-props :keywordize-keys true)]
    (r/as-element
      [:> ui/Typography (merge {:class (:pre classes)} props)])))

(defn game-page []
  (let [game (session/get :game)
        {:keys [id people roles]} game]
    (if game
      [:> container
       [col {:container true
             :justify   "center"}
        [:> ui/Typography {:variant       "h5"
                           :gutter-bottom true} "Waiting for players..."]]
       [col {:container true
             :justify   "center"}
        [:> ui/Typography {:variant "h6"} "Access code: "]
        [:> pre {:variant "h6"} id]]
       [col
        (into [:> ui/List {:subheader (subheader-element "Players - " (count people))}]
              (for [player people]
                [:> ui/ListItem
                 [:> ui/ListItemIcon
                  [:> ui/IconButton {:on-click #(delete-player! id player)}
                   [:> icons/Delete]]]
                 [:> ui/ListItemText player]]))
        [:> ui/Divider]
        [:> ui/List {:subheader (subheader-element "Roles")}
         (for [[role desc] role-options]
           ^{:key role}
           [:> ui/ListItem [role-toggle id roles role desc]])]]
       [col {:xs 8}
        [:> ui/Button {:color     "secondary"
                       :variant   "contained"
                       :fullWidth true
                       :on-click  #(start-game! id)}
         "Start"]]]
      [spinner])))

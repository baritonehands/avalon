(ns avalon.play.quests
  (:require [reagent.session :as session]
            [ajax.core :refer [POST DELETE]]
            [avalon.utils :refer [capitalize show-error]]
            [avalon.pages.games :as games]))

(defn state []
  (session/get ::state))

(defn open-dialog [quest]
  (session/put! ::state (merge quest {:picker #{}})))

(defn close-dialog []
  (session/remove! ::state))

(defn alert-state []
  (session/get ::alert))

(defn open-alert [alert]
  (let [defaults {:on-confirm     (constantly nil)
                  :on-cancel      (constantly nil)
                  :confirm-button "Confirm"
                  :cancel-button  "Cancel"}]
    (session/put! ::alert (merge defaults alert))))

(defn confirm-alert []
  ((:on-confirm (alert-state)))
  (session/remove! ::alert))

(defn cancel-alert []
  ((:on-cancel (alert-state)))
  (session/remove! ::alert))

(defn picker-selected [names]
  (session/assoc-in!
    [::state :picker]
    (into (sorted-set) names)))

(defn picker-valid? []
  (let [{:keys [size picker]} (state)]
    (= size (count picker))))

(defn handle-error [action]
  (fn [{:keys [status response]}]
    (condp = status
      422 (show-error (str "Unable to " action) (capitalize (first (val (first response)))))
      status (show-error (str "Unable to " action) "Unexpected error, please try again"))))

(defn create-vote! []
  (let [id (session/get-in [:game :id])
        names (-> (state) :picker)]
    (POST (str "/api/games/" id "/votes")
          {:params          {:type  :quest
                             :names names}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         (fn [_]
                              (games/refresh-game)
                              (close-dialog))
           :error-handler (handle-error "Start Quest")})))

(defn vote! [choice]
  (let [{:keys [id person-id]} (session/get :route-params)]
    (POST (str "/api/games/" id "/people/" person-id "/vote")
          {:params          {:choice choice}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         (fn [_]
                              (games/refresh-game))
           :error-handler   (handle-error "Send Quest Card")})))

(defn clear-vote! []
  (let [id (session/get-in [:game :id])]
    (DELETE (str "/api/games/" id "/votes")
            {:response-format :json
             :keywords?       true
             :handler         (fn [_]
                                (games/refresh-game)
                                (close-dialog))
             :error-handler (handle-error "Cancel Quest")})))

(defn clear-quest! [n]
  (let [id (session/get-in [:game :id])]
    (DELETE (str "/api/games/" id "/quests/" (inc n))
            {:response-format :json
             :keywords?       true
             :handler         (fn [game]
                                (games/refresh-game)
                                (close-dialog))
             :error-handler (handle-error "Undo Quest")})))

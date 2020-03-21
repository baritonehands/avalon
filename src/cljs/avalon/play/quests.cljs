(ns avalon.play.quests
  (:require [reagent.session :as session]
            [ajax.core :refer [POST DELETE]]))

(defn state []
  (session/get ::state))

(defn open-dialog [quest]
  (session/put! ::state (merge quest {:picker #{}})))

(defn close-dialog []
  (session/remove! ::state))

(defn toggle-pick [pname selected?]
  (session/update-in!
    [::state :picker]
    (fn [picked]
      (if selected?
        (conj picked pname)
        (disj picked pname)))))

(defn picker-valid? []
  (let [{:keys [size picker]} (state)]
    (= size (count picker))))

(defn create-quest! []
  (let [id (session/get-in [:game :id])
        names (-> (state) :picker)]
    (POST (str "/api/games/" id "/quests")
          {:params          {:names names}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         (fn [game]
                              (session/put! :game game)
                              (close-dialog))})))

(defn vote! [choice]
  (let [{:keys [id person-id]} (session/get :route-params)]
    (POST (str "/api/games/" id "/people/" person-id "/vote")
          {:params          {:choice choice}
           :format          :json
           :response-format :json
           :keywords?       true
           :handler         (fn [game]
                              (session/put! :game game))})))

(defn clear-quest! []
  (let [id (session/get-in [:game :id])]
    (DELETE (str "/api/games/" id "/quests")
            {:response-format :json
             :keywords?       true
             :handler         (fn [game]
                                (session/put! :game game)
                                (close-dialog))})))

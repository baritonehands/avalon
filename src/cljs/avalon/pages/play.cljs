(ns avalon.pages.play
  (:require [ajax.core :refer [GET]]
            [reagent.session :as session]
            [avalon.utils :refer [row col]]))

(defn get-info! [id person-id]
  (GET (str "/api/games/" id "/people/" person-id "/info")
       {:response-format :json
        :keywords?       true
        :handler         #(session/put! :info %)}))

(defn description [role]
  (let [name (name role)
        letter (-> name
                   (.charAt name 0)
                   (.toUpperCase))]
    [:h3 "Your role is " [:strong (str letter (.slice name 1))]]))

(defn play-page []
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
         (for [player (:info info)]
           [:div.player player])]]]
      [:h3.text-center "Loading..."])))

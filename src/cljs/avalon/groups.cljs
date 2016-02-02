(ns avalon.groups
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [material-ui.core :as ui :include-macros true]
            [accountant.core :as route]))

(defn group []
  (let [group (session/get :group)]
    [:div.row>div.col-xs-12.col-sm-4
     [:h3 (:name group)]

     [:h4 "Players"]

     (for [person (:people group)]
       [:div.player (:name person)])]))

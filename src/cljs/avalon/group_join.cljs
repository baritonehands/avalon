(ns avalon.group-join
  (:require [reagent.core :as r]
            [avalon.util :as util]
            [secretary.core :as route]))

(defonce state (r/atom {}))

(defn join-view []
  [:div
   [:input {:type "text" :on-input (util/set-prop state :group-name)}]
   [:div (:group-name @state)]

   [:input {:type "text" :on-input (util/set-prop state :group-code)}]
   [:div (:group-code @state)]

   [:input {:type "button" :value "Join" :on-click #(route/dispatch! "/about")}]])
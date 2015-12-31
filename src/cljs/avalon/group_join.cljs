(ns avalon.group-join
  (:require [reagent.core :as r]
            [avalon.util :as util]
            [secretary.core :as route]))

(defonce state (r/atom {}))

(defn join-view []
  [:section.section--center.mdl-grid
   [:div.mdl-cell.mdl-cell--6-col
     [:div.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label
      [:input#name.mdl-textfield__input {:type "text" :on-input (util/set-prop state :group-name)}]
      [:span.mdl-textfield__label {:for "name"} "Group Name"]]
     [:div (:group-name @state)]]

   [:div.mdl-cell.mdl-cell--6-col
     [:div.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label
      [:input#code.mdl-textfield__input {:type "text" :on-input (util/set-prop state :group-code)}]
      [:span.mdl-textfield__label {:for "code"} "Password"]]
     [:div (:group-code @state)]]

   [:div.mdl-cell.mdl-cell--6-col
     [:input.mdl-button.mdl-js-button.mdl-button--raised.mdl-button--accent
      {:type "button" :value "Join" :on-click #(route/dispatch! "/about")}]]])
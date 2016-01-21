(ns avalon.group-join
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [material-ui.core :as ui :include-macros true]
            ;[avalon.util :as util]
            [accountant.core :as route]))

(defonce state (r/atom {}))

(defn join-template []
  [:div
   [:div
     [ui/TextField {:hintText "Please enter a group name"
                    :floatingLabelText "Group Name"
                    :value (:group-name @state)
                    :on-change #(swap! state assoc :group-name (-> % .-target .-value))
                    }]]

   [:div
     [ui/TextField {:hintText "Please enter a passcode"
                    :floatingLabelText "Password"
                    :value (:group-code @state)
                    :type "password"
                    :on-change #(swap! state assoc :group-code (-> % .-target .-value))
                    }]]

   [:div
    [ui/RaisedButton {:primary true
                      :label "Join"
                      :on-click #(js/alert "Clicked!")
                      }]]])

(defn join-form []
  [:div
    [join-template]
    [:div (:group-name @state)]
    [:div (:group-code @state)]])
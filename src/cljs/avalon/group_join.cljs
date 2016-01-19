(ns avalon.group-join
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [material-ui.core :as ui :include-macros true]
            ;[avalon.util :as util]
            [accountant.core :as route]))

(defonce state (r/atom {}))

(def join-template
  [:section.section--center.mdl-grid
   [:div.mdl-cell.mdl-cell--6-col
    [:div.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label
     [ui/TextField {:hintText "Please enter a group name"
                    :floatingLabelText "Group Name"
                    :value (:group-name @state)
                    :on-change #(swap! state assoc :group-name (-> % .-target .-value))
                    }]]]

   [:div.mdl-cell.mdl-cell--6-col

    [:div.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label
     [ui/TextField {:hintText "Please enter a passcode"
                    :floatingLabelText "Password"
                    :value (:group-code @state)
                    :on-change #(swap! state assoc :group-code (-> % .-target .-value))
                    }]]]

   [:div.mdl-cell.mdl-cell--6-col
    [ui/RaisedButton {:primary true
                      :label "Join"
                      :on-click #(js/alert "Clicked!")
                      }]]])

(defn join-form []
  (let [doc (r/atom {:group-name "GrubHub" :group-code "merlinity"})]
    (fn []
      [:div
        [bind-fields join-template doc]
        [:div (:group-name @state)]
        [:div (:group-code @state)]])))
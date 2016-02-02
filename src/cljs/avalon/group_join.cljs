(ns avalon.group-join
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [reagent-forms.core :refer [bind-fields]]
            [material-ui.core :as ui :include-macros true]
    ;[avalon.util :as util]
            [accountant.core :as route]
            [ajax.core :refer [POST]]))

(defn join-handler [response]
  (session/put! :group response)
  (route/navigate! (str "/groups/" (:id response))))

(defn join-error [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn join-group [name code]
  (POST "/api/groups/join" {:params          {:name name
                                              :code code}
                            :format          :json
                            :response-format :json
                            :keywords?       true
                            :handler         join-handler
                            :error-handler   join-error}))

(defn join-form []
  (let [state (r/atom {})]
    (fn []
      [:div.row
       [:div.col-xs-12.col-sm-4
        [:div.form-group
         [:div
          [ui/TextField {:hintText          "Please enter a group name"
                         :floatingLabelText "Group Name"
                         :value             (:group-name @state)
                         :on-change         #(swap! state assoc :group-name (-> % .-target .-value))
                         }]]

         [:div
          [ui/TextField {:hintText          "Please enter a passcode"
                         :floatingLabelText "Password"
                         :value             (:group-code @state)
                         :type              "password"
                         :on-change         #(swap! state assoc :group-code (-> % .-target .-value))
                         }]]]

        [:div
         [ui/RaisedButton {:primary  true
                           :label    "Join"
                           :on-click #(join-group (:group-name @state) (:group-code @state))
                           }]]]])))
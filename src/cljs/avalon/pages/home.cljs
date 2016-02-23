(ns avalon.pages.home
  (:require [reagent.core :as r]
            [avalon.utils :refer [row col]]
            [accountant.core :as route]
            [material-ui.core :as ui :include-macros true]))

(defn join-game [id]
  (route/navigate! (str "/games/" id)))

(defn button [label opts]
  [:div.col-xs-8.col-xs-offset-2
   [ui/RaisedButton (merge {:label     label
                            :primary   true
                            :className "start-btn"} opts)]])

(defn home-page []
  (let [state (r/atom {:joining false})]
    (fn []
      [:div.text-center
       [row
        [col
         [:h3 "Welcome to Avalon!"]]]
       [row
        [col
         (if-not (:joining @state)
           [row
            [button "Create Game" {}]
            [button "Join Game" {:on-click #(swap! state assoc :joining true)}]]

           [row
            [:div.col-xs-8.col-xs-offset-2

             [ui/TextField {:hintText          "Enter an access code"
                            :floatingLabelText "Access Code"
                            :value             (:code @state)
                            :on-change         #(swap! state assoc :code (-> % .-target .-value))
                            }]]

            [:div.col-xs-8.col-xs-offset-2

             [ui/TextField {:hintText          "Enter your name"
                            :floatingLabelText "Your Name"
                            :value             (:name @state)
                            :on-change         #(swap! state assoc :name (-> % .-target .-value))
                            }]]
            [row
             [button "Join" {:on-click #(join-game (:code @state))}]
             [button "Back" {:on-click #(swap! state assoc :joining false)
                             :primary  false}]]])]]])))
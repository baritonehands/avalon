(ns avalon.play.quest-vote
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [material-ui-icons :as icons]
            [avalon.utils :refer [col]]
            [reagent.session :as session]
            [clojure.string :as s]))

(defn buttons [{:keys [selection]}]
  [:> ui/Grid {:container true
               :spacing   2
               :justify   "space-between"}
   [col {:xs 6}
    [:> ui/Button {:variant    (if (= @selection :failure)
                                 "contained"
                                 "outlined")
                   :color      "secondary"
                   :full-width true
                   :on-click   #(reset! selection :failure)}
     [:> icons/HighlightOffOutlined
      {:font-size "large"}]]]
   [col {:xs 6}
    [:> ui/Button {:variant    (if (= @selection :success)
                                 "contained"
                                 "outlined")
                   :color      "primary"
                   :full-width true
                   :on-click   #(reset! selection :success)}
     [:> icons/CheckCircleOutlineOutlined
      {:font-size "large"}]]]])

(defn view [_]
  (let [selection (r/atom nil)]
    (fn [{:keys [n close] :as props}]
      (if-let [vote (:vote (session/get :info))]
        [:> ui/Dialog {:open       true
                       :max-width  "sm"
                       :full-width true}
         [:> ui/DialogTitle (str "Quest " (inc n))]
         [:> ui/DialogContent
          [:> ui/Typography {:variant "subtitle1"}
           "Participants"]
          [:> ui/Typography {:variant   "body1"
                             :paragraph true}
           (s/join ", " (sort (:people vote)))]
          [buttons {:vote      vote
                    :selection selection}]]
         [:> ui/DialogActions
          [:> ui/Button {:color    "secondary"
                         :on-click close}
           "Cancel Vote"]
          [:> ui/Button {:color    "primary"
                         :disabled (not @selection)
                         :on-click close}
           "Send Vote"]]]))))

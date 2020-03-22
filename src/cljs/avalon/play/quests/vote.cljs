(ns avalon.play.quests.vote
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [material-ui-icons :as icons]
            [avalon.utils :refer [col]]
            [reagent.session :as session]
            [clojure.string :as s]
            [avalon.play.quests :as quests]))

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
      (if-let [vote (session/get-in [:info :vote])]
        [:> ui/Dialog {:open       true
                       :max-width  "sm"
                       :full-width true}
         [:> ui/DialogTitle (str "Quest " (inc n))]
         [:> ui/DialogContent
          [:> ui/Grid {:container true
                       :spacing   2}
           [:> ui/Typography {:variant "subtitle1"}
            "Participants:"]
           (into
             [col {:container true
                   :spacing   1}]
             (for [player (sort (:people vote))]
               [:> ui/Chip {:label player}]))
           (if (:participant vote)
             [:<>
              [:> ui/Typography {:variant "subtitle1"}
               "Vote:"]
              [buttons {:vote      vote
                        :selection selection}]]
             [:<>
              [:> ui/Typography {:variant "subtitle1"}
               "Waiting For:"]
              (into
                [col {:container true
                      :spacing   1}]
                (for [player (sort (:waiting vote))]
                  [:> ui/Chip {:label player}]))])]]
         [:> ui/DialogActions
          [:> ui/Button {:color    "default"
                         :on-click (fn []
                                     (quests/open-alert {:title          "Cancel Vote"
                                                         :message        "Are you sure you want to cancel this vote?"
                                                         :confirm-button "Cancel Vote"
                                                         :cancel-button  "Keep Voting"
                                                         :on-confirm     (fn []
                                                                           (quests/clear-vote!)
                                                                           (reset! selection nil))}))}
           "Cancel Vote"]
          (if (:participant vote)
            [:> ui/Button {:color    "primary"
                           :disabled (not @selection)
                           :on-click (fn []
                                       (quests/vote! @selection)
                                       (reset! selection nil))}
             "Send Vote"])]]))))

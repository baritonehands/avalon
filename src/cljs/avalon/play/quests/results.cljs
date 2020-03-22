(ns avalon.play.quests.results
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [material-ui-icons :as icons]
            [avalon.utils :refer [col]]
            [clojure.string :as s]
            [avalon.play.quests :as quests]
            [avalon.rules.quests :as rules]))

(defn props->color [js-props]
  (let [color (aget js-props "color")
        palette (-> js-props (aget "theme") (aget "palette"))]
    (-> palette (aget color))))

(def card-styles
  #js {:root #js {:background #(-> % (props->color) (aget "main"))
                  :color      #(-> % (props->color) (aget "contrastText"))
                  :text-align "center"}})

(def with-card-styles (ui/withStyles card-styles))

(def count-styles
  #js {:root #js {:padding        "2px"
                  :vertical-align "middle"}})

(def count-text
  ((ui/withStyles count-styles) ui/Typography))
(def success-icon
  ((ui/withStyles count-styles) icons/CheckCircleOutlineOutlined))
(def failure-icon
  ((ui/withStyles count-styles) icons/HighlightOffOutlined))

(def icon-styles
  #js {:root #js {:vertical-align "middle"}})

(defn card [{:keys [classes result color] :as props}]
  [:> ui/Card {:class (.-root classes)}
   [:> ui/CardContent
    (if (= color "primary")
      [:<>
       [:> count-text {:variant "h5"
                       :display "inline"} (or (aget result "success") 0)]
       [:> success-icon {:font-size "large"}]]
      [:<>
       [:> count-text {:variant "h5"
                       :display "inline"} (or (aget result "failure") 0)]
       [:> failure-icon {:font-size "large"}]])]])

(def color-card
  (-> card
      (r/reactify-component)
      (with-card-styles)
      (ui/withTheme)))

(defn view []
  (let [{:keys [player-count size n result]} (quests/state)
        {:keys [success failure people]} result]
    [:> ui/Grid {:container true
                 :spacing   2}
     [:> ui/Typography {:variant "subtitle1"}
      "Participants:"]
     (into
       [col {:container true
             :spacing   1}]
       (for [player (sort people)]
         [:> ui/Chip {:label player}]))
     [:> ui/Typography {:variant "subtitle1"}
      "Votes:"]
     [:> ui/Grid {:container true
                  :spacing   2
                  :justify   "space-between"}
      [col {:xs 6}
       [:> color-card {:color  "primary"
                       :result result}]]
      [col {:xs 6}
       [:> color-card {:color  "secondary"
                       :result result}]]]]))

(def clear-message "Are you sure you want to undo this quest? All following quests will also be undone.")

(defn actions []
  (let [{:keys [n]} (quests/state)]
    [:<>
     [:> ui/Button {:color    "default"
                    :on-click (fn []
                                (quests/open-alert {:title          "Undo Quest"
                                                    :message        clear-message
                                                    :confirm-button "Undo Quest"
                                                    :cancel-button  "Keep Quest"
                                                    :on-confirm     #(quests/clear-quest! n)}))}
      "Undo Quest"]
     [:> ui/Button {:color    "primary"
                    :on-click #(quests/close-dialog)}
      "Dismiss"]]))

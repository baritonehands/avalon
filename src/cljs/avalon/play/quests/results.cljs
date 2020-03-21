(ns avalon.play.quests.results
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [material-ui-icons :as icons]
            [avalon.utils :refer [col]]
            [clojure.string :as s]
            [avalon.play.quests :as quests]))

(defn props->color [js-props]
  (let [color (aget js-props "color")
        palette (-> js-props (aget "theme") (aget "palette"))]
    (-> palette (aget color))))

(def card-styles
  #js {:root #js {:background #(-> % (props->color) (aget "main"))
                  :color      #(-> % (props->color) (aget "contrastText"))}})

(def with-card-styles (ui/withStyles card-styles))

(defn card [{:keys [classes result color] :as props}]
  [:> ui/Card {:class (.-root classes)}
   [:> ui/CardContent
    (if (= color "primary")
      [:<>
       [:> ui/Typography {:variant "h5"} (or (.-success result) 0)]
       [:> icons/CheckCircleOutlineOutlined {:font-size "large"}]]
      [:<>
       [:> ui/Typography {:variant "h5"} (or (.-failure result) 0)]
       [:> icons/HighlightOffOutlined {:font-size "large"}]])]])

(def color-card
  (-> card
      (r/reactify-component)
      (with-card-styles)
      (ui/withTheme)))

(defn view []
  (let [{:keys [result]} (quests/state)
        {:keys [success failure people]} result]
    [col
     [:> ui/Typography {:variant "subtitle1"}
      "Participants: " (s/join ", " (sort people))]
     [:> ui/Grid {:container true
                  :spacing   2
                  :justify   "space-between"}
      [col {:xs 6}
       [:> color-card
        {:color  "primary"
         :result result}]]
      [col {:xs 6}
       [:> color-card {:color  "secondary"
                       :result result}]]]]))

(defn actions []
  [:<>
   [:> ui/Button {:color    "secondary"
                  :on-click #(quests/clear-quest!)}
    "Clear Quest"]
   [:> ui/Button {:color    "primary"
                  :on-click #(quests/close-dialog)}
    "Close"]])

(ns avalon.play.seats
  (:require [reagent.core :as r]
            [avalon.utils :refer [col]]
            [material-ui :as ui]
            [material-ui-icons :as icons]))

(def expansion-styles
  #js {:root #js {:padding-right "16px"
                  :padding-left  "16px"}})

(def expansion-summary ((ui/withStyles expansion-styles) ui/ExpansionPanelSummary))
(def expansion-details ((ui/withStyles expansion-styles) ui/ExpansionPanelDetails))

(def list-item-avatar
  ((ui/withStyles
     #js {:root #js {:min-width "36px"}})
   ui/ListItemAvatar))

(def avatar
  ((ui/withStyles
     #js {:root #js {:height    "24px"
                     :width     "24px"
                     :font-size "16px"}})
   ui/Avatar))

(def styled-list
  ((ui/withStyles
     #js {:root #js {:min-width      "50%"
                     :padding-top    0
                     :padding-bottom 0}})
   ui/List))

(defn list-half [first-idx seats]
  (into
    [:> styled-list {:dense true}]
    (for [[idx seat] (map-indexed vector seats)]
      [:> ui/ListItem
       [:> list-item-avatar [:> avatar (+ first-idx idx 1)]]
       [:> ui/ListItemText seat]])))

(defn view [seats]
  [col
   [:> ui/ExpansionPanel
    [:> expansion-summary {:expand-icon (r/as-element [:> icons/ExpandMore])}
     [:> ui/Typography {:variant "subtitle1"} "Seating Order (for remote play)"]]
    [:> expansion-details
     [col {:container true
           :spacing   1}
      (let [[group1 group2] (split-at (/ (count seats) 2) seats)]
        [:<>
         [list-half 0 group1]
         [list-half (count group1) group2]])]]]])

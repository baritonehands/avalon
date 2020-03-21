(ns avalon.play.quests.view
  (:require [material-ui :as ui]
            [avalon.utils :refer [col]]
            [avalon.rules.quests :as rules]
            [avalon.play.quests :as quests]
            [avalon.play.quests.dialog :as quest-dialog]
            [avalon.play.quests.vote :as quest-vote]
            [reagent.core :as r]))

(def round-button
  ((ui/withStyles
     #js {:root #js {:font-size "24px"}})
   ui/Fab))

(defn single [{:keys [size n player-count selected? result]}]
  [:> ui/Grid {:item true :xs 2}
   [:> round-button
    {:color    (cond
                 (nil? result) "default"
                 (rules/failed? player-count n result) "secondary"
                 :else "primary")
     :size     "large"
     :disabled (and (nil? result) (not selected?))
     :on-click (fn []
                 (quests/open-dialog {:size   size
                                      :n      n
                                      :result result}))}
    size]])

(defn root [{:keys [people quests]}]
  [:<>
   [quest-dialog/view]
   [quest-vote/view {:n (count quests)}]
   [:> ui/Card
    [:> ui/CardHeader {:title                  "Quests"
                       :title-typography-props {:variant "subtitle1"}}]
    [:> ui/CardContent
     (into
       [col {:container true
             :justify   "space-between"}]
       (for [[n size] (map-indexed vector (rules/counts (count people)))]
         [single {:size         size
                  :n            n
                  :player-count (count people)
                  :selected?    (= n (count quests))
                  :result       (get quests n)}]))]]])

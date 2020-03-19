(ns avalon.play.quests
  (:require [material-ui :as ui]
            [avalon.utils :refer [col]]
            [avalon.rules.quests :as rules]
            [avalon.play.quest-detail :as quest-detail]
            [reagent.core :as r]))

(def round-button
  ((ui/withStyles
     #js {:root #js {:font-size "24px"}})
   ui/Fab))

(defn single [{:keys [on-edit size selected? result]}]
  [:> ui/Grid {:item true :xs 2}
   [:> round-button
    {:color    (case result
                 "good" "primary"
                 "bad" "secondary"
                 "default")
     :size     "large"
     :disabled (and (nil? result) (not selected?))
     :on-click on-edit}
    size]])

(defn view [props]
  (let [to-edit (r/atom nil)
        close #(reset! to-edit nil)]
    (fn [{:keys [people quests]}]
      [:> ui/Grid {:container true
                   :spacing   1
                   :justify   "center"}
       [quest-detail/view @to-edit]
       [col
        [:> ui/Typography {:variant "h6"} "Quests:"]]
       (into
         [col {:container true
               :justify   "space-between"}]
         (for [[n size] (map-indexed vector (rules/counts (count people)))]
           (let [on-edit #(reset! to-edit {:n      n
                                           :result (get quests n)
                                           :close  close})]
             [single {:size      size
                      :selected? (= n (count quests))
                      :result    (get quests n)
                      :on-edit   on-edit}])))])))

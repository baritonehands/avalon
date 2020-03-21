(ns avalon.play.quests.dialog
  (:require [reagent.session :as session]
            [material-ui :as ui]
            [material-ui-icons :as icons]
            [avalon.utils :refer [col subheader-element form-control-label-full]]
            [clojure.string :as s]
            [reagent.core :as r]
            [avalon.play.quests.results :as quest-results]
            [avalon.play.quests.picker :as quest-picker]
            [avalon.play.quests :as quests]))

(defn view []
  (let [{:keys [n result] :as quest} (quests/state)
        completed? (boolean result)]
    (if quest
      [:> ui/Dialog {:open       true
                     :max-width  "sm"
                     :full-width true}
       [:> ui/DialogTitle (str "Quest " (inc n))]
       [:> ui/DialogContent
        (if completed?
          [quest-results/view]
          [quest-picker/view])]
       [:> ui/DialogActions
        [:> ui/Button {:color    (if completed?
                                   "secondary"
                                   "default")
                       :on-click #(quests/close-dialog)}
         (if completed?
           "Clear Quest"
           "Cancel")]
        [:> ui/Button {:color    "primary"
                       :on-click #(quests/close-dialog)
                       :disabled (not (quests/valid?))}
         (if result
           "Close"
           "Start Quest")]]])))

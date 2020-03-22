(ns avalon.play.quests.picker
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [avalon.utils :refer [subheader-element form-control-label-full]]
            [reagent.session :as session]
            [avalon.play.quests :as quests]))

(defn view []
  (let [{:keys [size picker]} (quests/state)
        people (session/get-in [:game :people])]
    [:> ui/FormControl {:full-width true}
     [:> ui/InputLabel
      {:id "quest-picker-label"}
      (str "Pick " size " Players")]
     (into
       [:> ui/Select {:label-id     "quest-picker-label"
                      :multiple     true
                      :value        picker
                      :on-change    #(quests/picker-selected (-> % .-target .-value))
                      :render-value (fn [selected]
                                      (r/as-element
                                        (into
                                          [:> ui/Grid {:container true}]
                                          (for [item selected]
                                            [:> ui/Chip {:label item}]))))}]

       (for [[idx player] (map-indexed vector (sort people))]
         [:> ui/MenuItem {:value player}
          player]))]))

(defn actions []
  [:<>
   [:> ui/Button {:color    "default"
                  :on-click #(quests/close-dialog)}
    "Cancel"]
   [:> ui/Button {:color    "primary"
                  :on-click #(quests/create-vote!)
                  :disabled (not (quests/picker-valid?))}
    "Start Quest"]])

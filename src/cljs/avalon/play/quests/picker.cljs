(ns avalon.play.quests.picker
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [avalon.utils :refer [subheader-element form-control-label-full]]
            [reagent.session :as session]
            [avalon.play.quests :as quests]))

(def form-control-half
  ((ui/withStyles
     #js {:root #js {:min-width "50%"}})
   ui/FormControl))

(defn view []
  (let [{:keys [size picker]} (quests/state)
        people (sort (session/get-in [:game :people]))
        error? (and (seq picker)
                    (not= (count picker) size))]
    (into
      [:<>
       [:> ui/DialogContentText
        (str "Choose " size " players")]]
      (for [group (split-at (/ (count people) 2) people)]
        [:> form-control-half {:component "fieldset"
                               :error     error?}
         (into
           [:> ui/FormGroup {:on-change (fn [event]
                                          (let [target (.-target event)]
                                            (quests/picker-selected (.-name target) (.-checked target))))}]
           (for [player group]
             [:> ui/FormControlLabel
              {:label   player
               :control (r/as-element
                          [:> ui/Checkbox {:name  player
                                           :color "primary"}])}]))]))))

(defn actions []
  [:<>
   [:> ui/Button {:color    "default"
                  :on-click #(quests/close-dialog)}
    "Cancel"]
   [:> ui/Button {:color    "primary"
                  :on-click #(quests/create-vote!)
                  :disabled (not (quests/picker-valid?))}
    "Start Quest"]])

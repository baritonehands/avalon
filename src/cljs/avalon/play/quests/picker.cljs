(ns avalon.play.quests.picker
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [avalon.utils :refer [subheader-element form-control-label-full]]
            [reagent.session :as session]
            [avalon.play.quests :as quests]))

(defn view []
  (let [{:keys [size]} (quests/state)
        people (session/get-in [:game :people])]
    [:<>
     (into
       [:> ui/List {:disable-padding true
                    :subheader       (subheader-element
                                       {:disable-sticky  false
                                        :disable-gutters true}
                                       (str "Pick " size " players:"))}]
       (for [[idx player] (map-indexed vector (sort people))]
         [:> ui/ListItem {:disable-gutters true}
          [:> form-control-label-full
           {:label           player
            :label-placement "start"
            :control         (r/as-element
                               [:> ui/Switch {:checked   (-> (quests/state) :picker (contains? player))
                                              :on-change #(quests/toggle-pick player (-> % .-target .-checked))
                                              :color     "primary"}])}]]))]))

(defn actions []
  [:<>
   [:> ui/Button {:color    "default"
                  :on-click #(quests/close-dialog)}
    "Cancel"]
   [:> ui/Button {:color    "primary"
                  :on-click #(quests/create-vote!)
                  :disabled (not (quests/picker-valid?))}
    "Start Quest"]])

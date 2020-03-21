(ns avalon.play.quests.picker
  (:require [reagent.core :as r]
            [material-ui :as ui]
            [avalon.utils :refer [subheader-element form-control-label-full]]
            [reagent.session :as session]
            [avalon.play.quests :as quests]))

(defn view []
  (let [selected (r/atom #{})]
    (fn []
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
                                   [:> ui/Switch {:checked   (contains? @selected idx)
                                                  :on-change (fn [event]
                                                               (let [op (if (-> event .-target .-checked) conj disj)]
                                                                 (swap! selected op idx)))
                                                  :color     "primary"}])}]]))]))))

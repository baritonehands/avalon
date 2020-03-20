(ns avalon.play.quest-detail
  (:require [reagent.session :as session]
            [material-ui :as ui]
            [avalon.utils :refer [subheader-element]]))

(defn results [winner]
  [:> ui/Typography {:variant "h2"} winner])

(defn pick [{:keys [n size]}]
  (let [{:keys [people]} (session/get :game)]
    [:<>
     (into
       [:> ui/List {:disable-padding true
                    :subheader       (subheader-element
                                       {:disable-sticky  false
                                        :disable-gutters true}
                                       (str "Pick " size " players:"))}]
       (for [[idx player] (map-indexed vector (sort people))]
         [:> ui/ListItem {:disable-gutters true}
          [:> ui/ListItemIcon
           [:> ui/Switch {:checked (= idx 2)
                          :color   "primary"
                          :edge    "start"}]]
          [:> ui/ListItemText {:primary player}]]))]))

(defn view [{:keys [n result close] :as props}]
  (if props
    [:> ui/Dialog {:open       true
                   :max-width  "sm"
                   :full-width true}
     [:> ui/DialogTitle (str "Quest " (inc n))]
     [:> ui/DialogContent
      (if result
        [results result]
        [pick props])]
     [:> ui/DialogActions
      [:> ui/Button {:color    "default"
                     :on-click close}
       "Cancel"]
      [:> ui/Button {:color    "primary"
                     :on-click close}
       "OK"]]]))

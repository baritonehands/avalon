(ns avalon.play.quest-detail
  (:require [reagent.session :as session]
            [material-ui :as ui]))

(defn results [winner]
  [:> ui/Typography {:variant "h2"} winner])

(defn pick [n]
  (let [{:keys [people]} (session/get :game)]
    [:<>
     [:> ui/Typography {:variant "h5"} (str "Pick " n " players:")]
     (into
       [:> ui/List]
       (for [[idx player] (map-indexed vector (sort people))]
         [:> ui/ListItem
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
        [pick n])]
     [:> ui/DialogActions
      [:> ui/Button {:color    "default"
                     :on-click close}
       "Cancel"]
      [:> ui/Button {:color    "primary"
                     :on-click close}
       "OK"]]]
    [:div "No editing"]))

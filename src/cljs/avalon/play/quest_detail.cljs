(ns avalon.play.quest-detail
  (:require [reagent.session :as session]
            [material-ui :as ui]
            [material-ui-icons :as icons]
            [avalon.utils :refer [col subheader-element]]
            [clojure.string :as s]
            [reagent.core :as r]))

(defn props->color [js-props]
  (let [color (aget js-props "color")
        palette (-> js-props (aget "theme") (aget "palette"))]
    (-> palette (aget color))))

(def card-styles
  #js {:root #js {:background #(-> % (props->color) (aget "main"))
                  :color      #(-> % (props->color) (aget "contrastText"))}})

(def with-card-styles (ui/withStyles card-styles))

(defn card [{:keys [classes result color] :as props}]
  (println result)
  [:> ui/Card {:class (.-root classes)}
   [:> ui/CardContent
    (if (= color "primary")
      [:<>
       [:> ui/Typography {:variant "h5"} (or (.-success result) 0)]
       [:> icons/CheckCircleOutlineOutlined {:font-size "large"}]]
      [:<>
       [:> ui/Typography {:variant "h5"} (or (.-failure result) 0)]
       [:> icons/HighlightOffOutlined {:font-size "large"}]])]])

(def color-card
  (-> card
      (r/reactify-component)
      (with-card-styles)
      (ui/withTheme)))

(defn results [{:keys [n size result]}]
  (let [{:keys [success failure people]} result]
    [col
     [:> ui/Typography {:variant "subtitle1"}
      "Participants: " (s/join ", " (sort people))]
     [:> ui/Grid {:container true
                  :spacing   2
                  :justify   "space-between"}
      [col {:xs 6}
       [:> color-card
        {:color  "primary"
         :result result}]]
      ;[:<>
      ; [:> icons/CheckCircleOutlineOutlined
      ;  {:font-size "large"}]]]

      [col {:xs 6}
       [:> color-card {:color  "secondary"
                       :result result}]]]]))
;[:<>
; [:> icons/HighlightOffOutlined
;  {:font-size "large"}]]]]]))

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
        [results props]
        [pick props])]
     [:> ui/DialogActions
      [:> ui/Button {:color    "default"
                     :on-click close}
       "Cancel"]
      [:> ui/Button {:color    "primary"
                     :on-click close}
       "OK"]]]))

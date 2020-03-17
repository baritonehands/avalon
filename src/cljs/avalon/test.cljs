(ns avalon.test
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]
            [material-ui :as ui]))

;; -------------------------
;; Views

(defn base-layout [& children]
  [:div
   ;[:> ui/AppBar
   ; [:> ui/Toolbar
   ;  [:> ui/Link {:title   "Avalon"
   ;               :href    "/"
   ;               :variant "h6"}]]]
   (into [:div.container-fluid] children)])

(defn current-page []
  (reagent/as-element
    [:> ui/Container {:max-width "sm"}
     ;[base-layout
     [:div "Body"]]))

(defn mount-root []
  (println "Mounting!")
  (rdom/render
    [:> current-page]
    (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))

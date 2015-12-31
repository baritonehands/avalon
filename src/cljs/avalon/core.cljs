(ns avalon.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [avalon.group-join :as gj]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Views

(defn home-page []
  [:div.mdl-layout.mdl-js-layout.mdl-layout--fixed-header
   [:header.mdl-layout__header
    [:div.mdl-layout__header-row [:span.mdl-layout__title "Welcome to Avalon!"]]]
   [:main.mdl-layout__content
    [:form {:action "#"}
      [gj/join-view]]]
   [:footer.mdl-mini-footer
    [:div.mdl-mini-footer__left-section
     [:ul.mdl-mini-footer__link-list [:li [:a {:href "/about"} "About"]]]]]])

(defn about-page []
  [:div [:h2 "About avalon"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))

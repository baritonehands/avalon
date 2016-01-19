(ns avalon.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [material-ui.core :as ui :include-macros true]
              [avalon.group-join :as gj]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Views

(defn home-page []
  [ui/AppCanvas {:predefinedLayout 1}
   [ui/AppBar {:class                    "mui-dark-theme"
               :title                    "Welcome to Avalon!"
               :zDepth                   0}
    [:div.action-icons
     [ui/IconButton {:iconClassName "mdfi_navigation_more_vert"}]
     [ui/IconButton {:iconClassName "mdfi_action_favorite_outline"}]
     [ui/IconButton {:iconClassName "mdfi_action_search"}]]]
   [:div.mui-app-content-canvas
    [gj/join-form]]])

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

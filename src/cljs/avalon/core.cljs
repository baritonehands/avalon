(ns avalon.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [material-ui.core :as ui :include-macros true]
            [avalon.pages.home :as join]
            [avalon.pages.groups :as groups]
            [avalon.pages.games :as games]
            [avalon.pages.play :as play]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

;; -------------------------
;; Views

(defn base-layout [& children]
  [ui/AppCanvas {:predefinedLayout 1}
   [ui/AppBar {:class              "mui-dark-theme"
               :title              "Avalon"
               :zDepth             0
               :showMenuIconButton false}]
   ;[:div.action-icons
   ; [ui/IconButton {:iconClassName "mdfi_navigation_more_vert"
   ;                 :on-click #(accountant/navigate! "/about")}]
   ; [ui/IconButton {:iconClassName "mdfi_action_favorite_outline"}]
   ; [ui/IconButton {:iconClassName "mdfi_action_search"}]]]
   [:div.mui-app-content-canvas.container-fluid
    children]])

(defn home-page []
  [base-layout [join/home-page]])

(defn game-page []
  [base-layout [games/game-page]])

(defn play-page []
  [base-layout [play/play-page]])

(defn group-page []
  [base-layout [groups/group]])

(defn about-page []
  [base-layout [:div [:h2 "About avalon"]
                [:div [:a {:href "/"} "go to the home page"]]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Handlers



;; -------------------------
;; Routes

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

(secretary/defroute "/games/:id" [id]
                    (games/get-game! id)
                    (session/put! :current-page #'game-page))

(secretary/defroute "/games/:id/play/:person-id" [id person-id]
                    (play/get-info! id person-id)
                    (session/put! :current-page #'play-page))

(secretary/defroute "/groups/:id" [id]
                    (session/put! :current-page #'group-page))

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

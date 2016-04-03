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
  (let [error (session/get :error)]
    [:div
     [ui/AppBar {:title              "Avalon"
                 :onTitleTouchTap    #(accountant/navigate! "/")
                 :zDepth             0
                 :showMenuIconButton false}]
     [:div.container-fluid
      children]
     [ui/Dialog {:title (:title error)                      ;"Unable to Start Game"
                 :open  error
                 :style {:max-width "500px"}}
      [:div (:message error)]
      [:div [ui/FlatButton {:label      "OK"
                            :primary    true
                            :onTouchTap #(session/put! :error nil)
                            :style      {:float "right"}}]]]]))

(defn home-page []
  [base-layout [join/home-page]])

(defn play-page []
  [base-layout [play/play-page]])

(defn group-page []
  [base-layout [groups/group]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Handlers



;; -------------------------
;; Routes

(defn set-page!
  ([current]
   (set-page! current {}))
  ([current kws]
   (session/put! :current-page current)
   (session/put! :route-params kws)))

(secretary/defroute "/" []
                    (set-page! #'home-page))

(secretary/defroute "/games/:id/play/:person-id" [id person-id]
                    (games/get-game! id)
                    (games/get-info! id person-id)
                    (set-page! #'play-page {:id id :person-id person-id}))

(secretary/defroute "/groups/:id" [id]
                    (set-page! #'group-page {:id id}))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))

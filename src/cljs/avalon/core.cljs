(ns avalon.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]
            [reagent.session :as session]
            [material-ui :as ui]
            [avalon.pages.home :as join]
            [avalon.pages.games :as games]
            [avalon.pages.play :as play]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

;; -------------------------
;; Views

(def theme
  (ui/createMuiTheme
    #js {:palette #js {:primary   (.-blue ui/colors)
                       :secondary #js {:main "#ff5252"}}}))

(defn base-layout [& children]
  (let [error (session/get :error)]
    [:> ui/ThemeProvider {:theme theme}
     [:> ui/CssBaseline]
     [:> ui/AppBar {:color    "primary"
                    :position "static"}
      [:> ui/Toolbar
       [:> ui/Link {:href      "/"
                    :color     "inherit"
                    :underline "none"
                    :variant   "h5"}
        "Avalon"]]]
     (into [:> ui/Container {:max-width       "sm"
                             :disable-gutters true}] children)
     (if error
       [:> ui/Dialog {:open       true
                      :max-width  "sm"
                      :full-width true}
        [:> ui/DialogTitle (:title error)]
        [:> ui/DialogContent (:message error)]
        [:> ui/DialogActions
         [:> ui/Button {:color    "primary"
                        :on-click #(session/put! :error nil)
                        :style    {:float "right"}}
          "OK"]]])]))

(defn home-page []
  [base-layout [join/home-page]])

(defn play-page []
  [base-layout [play/play-page]])

(defn current-page []
  (reagent/as-element
    [(session/get :current-page)]))

;; -------------------------
;; Handlers



;; -------------------------
;; Routes

(defn set-page!
  ([current]
   (set-page! current {}))
  ([current kws]
   (when (.-ga js/window)
     (.ga js/window "set" "page" (name (-> current meta :name)))
     (.ga js/window "send" "pageview"))
   (session/put! :current-page current)
   (session/put! :route-params kws)))

(secretary/defroute "/" []
                    (set-page! #'home-page))

(secretary/defroute "/games/:id/play/:person-id" [id person-id]
                    (games/get-game! id)
                    (games/get-info! id person-id)
                    (set-page! #'play-page {:id id :person-id person-id}))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [:> current-page] (.getElementById js/document "app")))

(defn ^:export init []
  (accountant/configure-navigation!
    {:nav-handler secretary/dispatch!})
  (accountant/dispatch-current!)
  (mount-root))

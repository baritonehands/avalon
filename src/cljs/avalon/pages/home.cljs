(ns avalon.pages.home
  (:require [ajax.core :refer [POST]]
            [reagent.core :as r]
            [avalon.utils :refer [row col show-error make-styles]]
            [avalon.pages.games :as games]
            [accountant.core :as route]
            [material-ui :as ui]
            [reagent.session :as session]))

(defn join-game! [id name]
  (POST (str "/api/games/" id "/people")
        {:params          {:name name}
         :format          :json
         :response-format :json
         :keywords?       true
         :handler         (fn [resp]
                            (session/put! :person-id (:id resp))
                            (games/get-game! id :force true)
                            (route/navigate! (str "/games/" id "/play/" (:id resp))))
         :error-handler   (fn [{:keys [response]}]
                            (show-error "Unable to join game" (-> response first second first)))}))

(def use-styles
  (make-styles
    (fn [theme]
      {:button     {:margin-bottom (.spacing theme 2)}
       :header     {:padding (.spacing theme 2)}
       :text-field {:margin-bottom (.spacing theme 2)}})))

(defn header []
  (let [classes (use-styles)]
    (r/as-element
      [:> ui/Typography {:variant "h5"
                         :class   (:header classes)}
       "Welcome to Avalon!"])))

(defn button [js-props]
  (let [classes (use-styles)
        props (js->clj js-props :keywordize-keys true)]
    (r/as-element
      [:> ui/Grid {:item true :xs 8}
       [:> ui/Button (merge {:variant    "contained"
                             :color      "secondary"
                             :full-width true
                             :class      (:button classes)} props)]])))

(defn text-field [js-props]
  (let [classes (use-styles)
        props (js->clj js-props :keywordize-keys true)]
    (r/as-element
      [:> ui/Grid {:item true :xs 8}
       [:> ui/TextField (merge {:full-width true
                                :class      (:text-field classes)} props)]])))

(defn home-page []
  (let [state (r/atom {:joining false})
        create! (fn [_]
                  (POST "/api/games"
                        {:response-format :json
                         :keywords?       true
                         :handler         (fn [resp]
                                            (session/put! :game resp)
                                            (swap! state assoc :joining true :code (:id resp)))}))]
    (fn []
      [:> ui/Grid {:container true}
       [col {:container true
             :justify   "center"}
        [:> header]]
       [col {:container true
             :justify   "center"}
        (if-not (:joining @state)
          [:<>
           [:> button {:onClick create!} "Create Game"]
           [:> button {:onClick #(swap! state assoc :joining true)} "Join Game"]]

          [:<>
           [col {:container true
                 :justify   "center"}
            [:> text-field {:placeholder   "Enter an access code"
                            :label         "Access Code"
                            :full-width    true
                            :input-props   {:auto-capitalize "none"
                                            :auto-correct    "off"}
                            :default-value (:code @state)
                            :on-change     #(swap! state assoc :code (-> % .-target .-value))}]]


           [col {:container true
                 :justify   "center"}

            [:> text-field {:placeholder   "Enter your name"
                            :label         "Your Name"
                            :full-width    true
                            :input-props   {:auto-correct "off"}
                            :default-value (:name @state)
                            :on-change     #(swap! state assoc :name (-> % .-target .-value))}]]

           [:> button {:on-click #(join-game! (:code @state) (:name @state))} "Join"]
           [:> button {:on-click #(swap! state assoc :joining false)
                       :color    "default"} "Back"]])]])))
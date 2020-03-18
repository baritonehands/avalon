(ns avalon.pages.play
  (:require [ajax.core :refer [POST]]
            [reagent.core :as r]
            [reagent.session :as session]
            [avalon.utils :refer [row col spinner capitalize show-error make-styles]]
            [avalon.pages.games :as games]
            [material-ui :as ui]
            [accountant.core :as route]))

(def twins #{"twin1" "twin2"})
(def good-lancelot #{"good-lancelot1" "good-lancelot2"})
(def evil-lancelot #{"evil-lancelot1" "evil-lancelot2"})

(defn desc-text [& children]
  (into
    [:> ui/Typography {:variant "h6"}]
    children))

(defn description [role]
  (let [name (capitalize (name role))]
    (cond
      (= role "good") [desc-text "You are a loyal servant of Arthur."]
      (= role "merlin") [desc-text "You are the great and powerful wizard " [:strong name] "."]
      (= role "percival") [desc-text "You are " [:strong name]]
      (= role "mordred") [desc-text "You are the dark lord " [:strong name] "."]
      (= role "morgana") [desc-text "You are the evil witch " [:strong name] "."]
      (= role "oberon") [desc-text "You are the evil force " [:strong name] "."]
      (= role "bad") [desc-text "You are a minion of Mordred."]
      (= role "assassin") [desc-text "You are the " [:strong name] " and a minion of Mordred."]
      (twins role) [desc-text "You are one of the " [:strong "Twins"] " and a loyal servant of Arthur."]
      (evil-lancelot role) [desc-text "You are " [:strong "Evil Lancelot"] " and a minion of Mordred."]
      (good-lancelot role) [desc-text "You are " [:strong "Good Lancelot"] " and a loyal servant of Arthur."]
      :else [desc-text "Your role is " [:strong name] "."])))

(defn sub [& children]
  (into
    [:> ui/Typography {:variant "subtitle2"}]
    children))

(defn backstory [role]
  (cond
    (= role "good") [sub "You are a simple servant with no knowledge of the others players. Who do you choose to trust?"]
    (= role "merlin") [sub "You have knowledge of all the evil players except Mordred. Find a way to make Percival trust you if you hope to win."]
    (= role "percival") [sub "You know two players: one is the evil witch, the other is the great wizard Merlin. You must decide which one to trust. Choose wisely."]
    (= role "mordred") [sub "You are a bad who is hidden to all good, including Merlin. Take care not to expose yourself and your evil intentions."]
    (= role "morgana") [sub "Merlin and your sinister teammates may know your intentions, but you can still fool Percival. How do you convince Percival that you are Merlin?"]
    (= role "oberon") [sub "You are a bad that is unknown to the rest of your team. Only Merlin knows you to be evil."]
    (= role "bad") [sub "You are an evil minion of Mordred. Merlin knows your true identity."]
    (= role "assassin") [sub "You are an evil minion of Mordred. Merlin knows your true identity."]
    (twins role) [sub "You are one of the twins. You two know yourselves to be good. Do you share your knowledge with the world?"]
    (= role "evil-lancelot1") [sub "You know the identity of good Lancelot, but he and Merlin know how evil you really are. How do you use this knowledge to win?"]
    (= role "good-lancelot1") [sub "You know the identity of evil Lancelot. How do you use this knowledge to win?"]
    (#{"evil-lancelot2" "good-lancelot2"} role) [sub "Your allegiance may change at the beginning of a round. Pray that fate leads you to victory."]
    :else [sub "You have been cast out of Avalon, unwanted by either Merlin or Mordred."]))

(defn text [& children]
  (into
    [:> ui/Typography {:variant   "body2"
                       :paragraph true
                       :component "div"}]
    children))

(defn goodbad [total]
  [text "There are " [:strong ([3 4 4 5 6 6] (- total 5))] " good players and " [:strong ([2 2 3 3 3 4] (- total 5))] " evil players in this game."])

(defn view-list [info]
  (let [role (:role info)]
    [col
     (into [:<>
            (cond
              (= role "merlin") [text "The following are the minions of Mordred:"]
              (= role "percival") [text "Merlin is one of the following:"]
              (= role "mordred") [text "The following are your minions:"]
              (= role "good-lancelot1") [text "The following is Evil Lancelot:"]
              (twins role) [text "The following is your twin and fellow good:"]
              (#{"morgana" "assassin" "bad" "evil-lancelot1"} role) [text "The following are Mordred and his other minions:"])]
           (for [player (first (:info info))]
             [:div.player player]))
     (if (= role "evil-lancelot1")
       [:div {:style {:padding-top "10px"}}
        [text "The following is Good Lancelot:"]
        [:div.player (first (second (:info info)))]])
     (if (and (contains? (set (session/get-in [:game :roles])) "lancelot2")
              (first (second (:info info))))
       [:div {:style {:padding-top "10px"}}
        [text "The following is Evil Lancelot:"]
        [:div.player (first (second (:info info)))]])]))

(defn handle-error [{:keys [status response]}]
  (condp = status
    422 (show-error "Unable to End Game" (capitalize (first (val (first response)))))
    status (show-error "Unable to End Game" "Unexpected error, please try again")))

(defn end-game! [id]
  (session/remove! :info)
  (POST (str "/api/games/" id "/reset")
        {:response-format :json
         :keywords?       true
         :handler         #(session/put! :game %)
         :error-handler   handle-error}))

(defn leave-game! [id]
  (session/remove! :info)
  (route/navigate! "/"))

(def use-styles
  (make-styles
    (fn [theme]
      {:container {:margin-top    (.spacing theme 2)
                   :margin-bottom (.spacing theme 4)}})))

(defn container [js-props]
  (let [classes (use-styles)]
    (r/as-element
      (into
        [:> ui/Grid {:container true
                     :justify   "center"
                     :spacing   2
                     :class     (:container classes)}]
        (.-children js-props)))))

(defn info-view [player-count]
  (let [info (session/get :info)
        params (session/get :route-params)]
    (if info
      [:> container
       [col
        [description (:role info)]]
       [col
        [backstory (:role info)]]
       [view-list info]
       [col
        [goodbad player-count]]
       [col
        [text [:strong (:first info)] " is the first player. The player to his/her right is the Lady of the Lake."]]
       (if (contains? (set (session/get-in [:game :roles])) "lancelot2")
         [col
          [text "Prepare and shuffle the " [:strong "Loyalty Deck"] " for Lancelot."
           [:ul
            [:li [:strong "Variant 1"] " - Three \"No Change\" cards, two \"Change Allegiance\" cards, draw a card in rounds 3, 4, and 5."]
            [:li [:strong "Variant 2"] " - Five \"No Change\" cards, two \"Change Allegiance\" cards, reveal a card for each round at game start. Evil Lancelot must always fail any quest."]]]])
       [col {:xs 8}
        [:> ui/Button {:fullWidth true
                       :variant   "outlined"
                       :on-click  #(end-game! (:id params))}
         "End Game"]]
       [col {:xs 8}
        [:> ui/Button {:fullWidth true
                       :variant   "outlined"
                       :on-click  #(leave-game! (:id params))}
         "Leave Game"]]]
      [spinner])))

(defn play-page []
  (let [timer (atom nil)]
    (r/create-class
      {:reagent-render         (fn []
                                 (let [game (session/get :game)]
                                   (if (= (:status game) "playing")
                                     [info-view (count (:people game))]
                                     [games/game-page])))
       :component-did-mount    #(reset! timer (js/setInterval games/refresh-game 5000))
       :component-will-unmount #(js/clearInterval @timer)})))


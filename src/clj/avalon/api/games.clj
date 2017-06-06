(ns avalon.api.games
  (:require [liberator.core :refer [defresource]]
            [liberator.representation :refer [ring-response]]
            [compojure.core :refer [defroutes ANY]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.games :as games]
            [avalon.models.groups :as groups]
            [avalon.rules.games :as rules]))

(defresource games-resource
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :malformed? (util/malformed? ::data)
             :processable? (fn [ctx]
                             (let [group-id (:groupId (::data ctx))]
                               (if (nil? group-id)
                                 true
                                 (crud/exists? groups/groups group-id))))
             :post! (fn [ctx]
                      (let [data (::data ctx)
                            game (games/create-game (:groupId data))]
                        {::game game}))
             :handle-created #(games/display-game (::game %)))

(defresource get-or-put-game [id]
             :available-media-types ["application/json"]
             :allowed-methods [:get :put]
             :exists? (crud/exists? games/games id)
             :can-put-to-missing? false
             :malformed? (util/malformed? ::data)
             :processable? (util/update-fields #{:roles :status} ::data)
             :handle-ok (games/display-game (crud/get games/games id))
             :put! #(crud/save! games/games id (::data %)))

(defresource games-by-group [id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :exists? (crud/exists? groups/groups id)
             :handle-ok (mapv games/display-game (filter #(= id (:group-id %)) (crud/all games/games))))

(defn start-game [game]
  (-> game
      (assoc :status :playing)
      (assoc :teams (rules/assign-roles game))
      (assoc :first (rand-nth (seq (:people game))))))

(defresource play-game [id]
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :exists? (crud/exists? games/games id)
             :can-post-to-missing? false
             :processable? (rules/valid-play? id ::errors)
             :handle-unprocessable-entity ::errors
             :post! (fn [_] (crud/update! games/games id start-game))
             :handle-created (fn [_] (games/display-game (crud/get games/games id))))

(defn toggle-lancelot [game k]
  (cond
    (= k :lancelot1) (update game :roles disj :lancelot2)
    (= k :lancelot2) (update game :roles disj :lancelot1)
    :else game))

(defn add-role [game f k]
  (-> (update game :roles f k)
      (toggle-lancelot k)))

(defn update-game [id name f]
  (fn [_]
    (crud/update! games/games id #(add-role % f (keyword name)))))

(defresource update-roles [id name]
             :available-media-types ["application/json"]
             :allowed-methods [:post :delete]
             :exists? (crud/exists? games/games id)
             :can-post-to-missing? false
             :processable? (rules/valid-role? id name ::errors)
             :handle-unprocessable-entity ::errors
             :new? false
             :respond-with-entity? true
             :post! (update-game id name conj)
             :delete! (update-game id name disj)
             :handle-ok (fn [_] (games/display-game (crud/get games/games id))))

(defroutes routes
           (ANY "/games" [] games-resource)
           (ANY "/games/:id" [id] (get-or-put-game (.toLowerCase id)))
           (ANY "/games/:id/play" [id] (play-game (.toLowerCase id)))
           (ANY "/games/:id/roles/:name" [id name] (update-roles (.toLowerCase id) name))
           (ANY "/groups/:id/games" [id] (games-by-group (.toLowerCase id))))

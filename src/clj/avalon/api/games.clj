(ns avalon.api.games
  (:require [liberator.core :refer [defresource]]
            [liberator.representation :refer [ring-response]]
            [compojure.core :refer [defroutes ANY]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.games :as games]
            [avalon.models.groups :as groups]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

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

(def play-game-rules
  {:people [[v/min-count 5]
            [v/max-count 10]]
   :status [[#{:waiting} :message "Game already started"]]})

(defn create-validator [rules]
  (fn [id]
    (let [game (crud/get games/games id)
          valid (b/valid? game rules)
          errors (first (b/validate game rules))]
      [valid {::errors errors}])))

(def valid-play? (create-validator play-game-rules))

(defresource play-game [id]
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :exists? (crud/exists? games/games id)
             :can-post-to-missing? false
             :processable? (valid-play? id)
             :handle-unprocessable-entity ::errors
             :post! (fn [_] (crud/update! games/games id #(assoc % :status :playing)))
             :handle-created (fn [_] (games/display-game (crud/get games/games id))))

(def update-roles-rules
  {:name [[v/member #{"merlin" "morgana" "percival" "mordred" "oberon"}]]
   :status [[#{:waiting} :message "Roles cannot be updated after game is started"]]})

(defn valid-role? [id name]
  (let [game (crud/get games/games id)
        to-validate {:name name :status (.status game)}
        valid (b/valid? to-validate update-roles-rules)
        errors (first (b/validate to-validate update-roles-rules))]
    [valid {::errors errors}]))

(defn update-game [id name f]
  (fn [_]
    (let [k (keyword name)]
      (crud/update! games/games id #(update % :roles f k)))))

(defresource update-roles [id name]
             :available-media-types ["application/json"]
             :allowed-methods [:post :delete]
             :exists? (crud/exists? games/games id)
             :can-post-to-missing? false
             :processable? (valid-role? id name)
             :handle-unprocessable-entity ::errors
             :new? false
             :respond-with-entity? true
             :post! (update-game id name conj)
             :delete! (update-game id name disj)
             :handle-ok (fn [_] (games/display-game (crud/get games/games id))))

(defroutes routes
  (ANY "/games" [] games-resource)
  (ANY "/games/:id" [id] (get-or-put-game id))
  (ANY "/games/:id/play" [id] (play-game id))
  (ANY "/games/:id/roles/:name" [id name] (update-roles id name))
  (ANY "/groups/:id/games" [id] (games-by-group id)))

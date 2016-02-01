(ns avalon.api.games
  (:require [liberator.core :refer [defresource]]
            [liberator.representation :refer [ring-response]]
            [compojure.core :refer [defroutes ANY]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.games :as games]
            [avalon.models.groups :as groups]))

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

(defroutes routes
  (ANY "/games" [] games-resource)
  (ANY "/games/:id" [id] (get-or-put-game id))
  (ANY "/groups/:id/games" [id] (games-by-group id)))

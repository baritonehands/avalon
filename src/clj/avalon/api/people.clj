(ns avalon.api.people
  (:require [liberator.core :refer [defresource resource]]
            [compojure.core :refer [defroutes ANY]]
            [clojure.pprint :refer [pprint]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.groups :as groups]
            [avalon.models.people :as people]
            [avalon.models.games :as games]
            [avalon.rules.people :as rules]))

(defn gen-endpoint [id db create-fn]
  (resource :available-media-types ["application/json"]
            :allowed-methods [:post]
            :exists? (crud/exists? db id)
            :can-post-to-missing? false
            :malformed? (util/malformed? ::data)
            :processable? (rules/valid-person? ::data ::errors)
            :handle-unprocessable-entity ::errors
            :post! (fn [ctx]
                     (let [data (::data ctx)
                           person (people/create-person (:name data))]
                       (create-fn id person)
                       {::id (:id person)}))
            :handle-created #(identity {:id (::id %)})))

(defn group-add-person [id] (gen-endpoint id groups/groups groups/add-person))
(defn game-add-person [id] (gen-endpoint id games/games games/add-person))

(defn get-people [sees teams]
  (into #{} (for [[k v] teams :when (sees v)]
              (:name (crud/get people/people k)))))

(defn get-info [game role]
  (let [teams (:teams game)
        evil (get-people #{:morgana :mordred :assassin :bad} teams)]
    (condp = role
      :merlin (get-people #{:morgana :bad :assassin :oberon} teams)
      :percival (get-people #{:morgana :merlin} teams)
      :mordred evil
      :morgana evil
      :assassin evil
      :bad evil
      :good []
      :oberon [])))

(defresource get-person-info [id person-id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :exists? (crud/exists? games/games id)
             :handle-ok (fn [_]
                          (let [game (crud/get games/games id)
                                role ((:teams game) person-id)]
                            {:role role
                             :first (= (:first game) person-id)
                             :info (get-info game role)})))

(defroutes routes
  (ANY "/groups/:id/people" [id] (group-add-person id))
  (ANY "/games/:id/people" [id] (game-add-person id))
  (ANY "/games/:id/people/:person-id" [id person-id] (get-person-info id person-id)))

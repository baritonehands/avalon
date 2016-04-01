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

(defn gen-endpoint [id db disp-fn delete-fn]
  (resource :available-media-types ["application/json"]
            :allowed-methods [:post :delete]
            :exists? (crud/exists? db id)
            :can-post-to-missing? false
            :malformed? (util/malformed? ::data)
            :processable? (rules/valid-person? ::data ::errors)
            :handle-unprocessable-entity ::errors
            :post! (fn [ctx]
                     (let [data (::data ctx)
                           person (people/create! (:name data))]
                       (crud/relate! db id :people (:id person))
                       {::id (:id person)}))
            :handle-created #(identity {:id (::id %)})
            :respond-with-entity? true
            :delete! (fn [ctx]
                       (let [name (:name (::data ctx))]
                         (delete-fn db id name)))
            :handle-ok (fn [_] (disp-fn (crud/get db id)))))

(defn group-add-person [id] (gen-endpoint id groups/groups groups/display people/delete!))
(defn game-add-person [id] (gen-endpoint id games/games games/display people/delete!))

(defn get-people [sees teams]
  [(into #{} (for [[person-id role] teams :when (sees role)]
              (:name (crud/get people/people person-id))))])

(defn get-info [game role person-id]
  (let [teams (dissoc (:teams game) person-id)
        evil-roles #{:morgana :mordred :assassin :bad :evil-lancelot}
        evil (get-people evil-roles teams)]
    (condp = role
      :merlin (get-people #{:morgana :bad :assassin :oberon :evil-lancelot} teams)
      :percival (get-people #{:morgana :merlin} teams)
      :twin1 (get-people #{:twin2} teams)
      :twin2 (get-people #{:twin1} teams)
      :mordred evil
      :morgana evil
      :assassin evil
      :bad evil
      :good #{}
      :oberon #{}
      :good-lancelot (get-people #{:evil-lancelot} teams)
      :evil-lancelot (concat evil (get-people #{:good-lancelot} teams)))))

(defresource get-person-info [id person-id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :exists? (crud/exists? games/games id)
             :processable? (rules/valid-info? (crud/get games/games id) ::errors)
             :handle-unprocessable-entity ::errors
             :handle-ok (fn [_]
                          (let [game (crud/get games/games id)
                                role ((:teams game) person-id)]
                            {:role role
                             :first (:name (crud/get people/people (:first game)))
                             :info (get-info game role person-id)})))

(defroutes routes
  (ANY "/groups/:id/people" [id] (group-add-person (.toLowerCase id)))
  (ANY "/games/:id/people" [id] (game-add-person (.toLowerCase id)))
  (ANY "/games/:id/people/:person-id/info" [id person-id] (get-person-info (.toLowerCase id) (.toLowerCase person-id))))

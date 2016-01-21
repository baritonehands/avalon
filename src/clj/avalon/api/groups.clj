(ns avalon.api.groups
  (:require [liberator.core :refer [defresource]]
            [compojure.core :refer [defroutes ANY]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.groups :as groups]
            [avalon.models.people :as people]))

(defresource groups-resource
             :available-media-types ["application/json"]
             :allowed-methods [:get :post]
             :malformed? (util/malformed? ::data)
             :processable? (util/require-fields [:name :code] ::data)
             :handle-ok (groups/display-all)
             :post! (fn [ctx]
                      (let [data (::data ctx)
                            group (groups/create-group (:name data) (:code data))]
                      {::id (:id group)}))
             :handle-created #(identity {:id (::id %)}))

(defresource get-group [id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :exists? (crud/exists? groups/groups id)
             :handle-ok (groups/display-group (crud/get groups/groups id)))

(defresource group-add-person [id]
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :exists? (crud/exists? groups/groups id)
             :can-post-to-missing? false
             :malformed? (util/malformed? ::data)
             :processable? (util/require-fields [:name] ::data)
             :post! (fn [ctx]
                      (dosync (let [data (::data ctx)
                                    person (people/create-person (:name data))]
                                (groups/add-person id person)))))

(defroutes routes
  (ANY "/groups" [] groups-resource)
  (ANY "/groups/:id" [id] (get-group id))
  (ANY "/groups/:id/people" [id] (group-add-person id)))

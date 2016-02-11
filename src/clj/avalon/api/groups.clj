(ns avalon.api.groups
  (:require [liberator.core :refer [defresource]]
            [compojure.core :refer [defroutes ANY]]
            [clojure.pprint :refer [pprint]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.groups :as groups]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

(def groups-resource-rules
  {:name v/required
   :code v/required})

(defresource groups-resource
             :available-media-types ["application/json"]
             :allowed-methods [:get :post]
             :malformed? (util/malformed? ::data)
             :processable? #(b/valid? (::data %) groups-resource-rules)
             :handle-unprocessable-entity #(first (b/validate (::data %) groups-resource-rules))
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

(defresource join-group
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :malformed? (util/malformed? ::data)
             :authorized? (fn [ctx]
                            (let [data (::data ctx)
                                  group (first (filter #(and (= (:name %) (:name data)) (= (:code %) (:code data)))
                                                    (crud/all groups/groups)))]
                              [group {::group group}]))
             :processable? (util/require-fields [:name :code] ::data)
             :handle-created #(groups/display-group (::group %)))

(defroutes routes
  (ANY "/groups" [] groups-resource)
  (ANY "/groups/join" [] join-group)
  (ANY "/groups/:id" [id] (get-group id)))

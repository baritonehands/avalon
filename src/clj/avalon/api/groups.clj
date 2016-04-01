(ns avalon.api.groups
  (:require [liberator.core :refer [defresource]]
            [compojure.core :refer [defroutes ANY]]
            [clojure.pprint :refer [pprint]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.groups :as groups]
            [avalon.rules.groups :as rules]))

(defresource groups-resource
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :malformed? (util/malformed? ::data)
             :post-to-existing? false
             :exists? #(groups/named (:name (::data %)))
             :processable? #(rules/valid-group? (::data %) ::error)
             :handle-unprocessable-entity ::error
             :post! (fn [ctx]
                      (let [data (::data ctx)
                            group (groups/create! (:name data) (:code data))]
                      {::id (:id group)}))
             :handle-ok "Group already exists by that name"
             :handle-created #(identity {:id (::id %)}))

(defresource get-group [id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :exists? (crud/exists? groups/groups id)
             :handle-ok (groups/display (crud/get groups/groups id)))

(defresource join-group
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :malformed? (util/malformed? ::data)
             :authorized? (fn [ctx]
                            (let [data (::data ctx)
                                  group (first (filter #(and (= (:name %) (:name data)) (= (:code %) (:code data)))
                                                    (crud/all groups/groups)))]
                              [group {::group group}]))
             :handle-created #(groups/display (::group %)))

(defroutes routes
  (ANY "/groups" [] groups-resource)
  (ANY "/groups/join" [] join-group)
  (ANY "/groups/:id" [id] (get-group id)))

(ns avalon.api.group
  (:require [liberator.core :refer [defresource]]
            [compojure.core :refer [defroutes ANY]]
            [avalon.api.util :as util]))

(defonce groups (ref {}))

(defn display-group [id group]
  (dissoc (assoc group :id id) :code))

(defresource groups-resource
             :available-media-types ["application/json"]
             :allowed-methods [:get :post]
             :malformed? (util/malformed? ::data)
             :processable? (util/require-fields [:name :code] ::data)
             :handle-unprocessable-entity {:message "Unprocessable"}
             :handle-ok (vec (for [[k v] @groups] (display-group k v)))
             :post! #(let [id (str (inc (rand-int 100000)))]
                      (dosync (alter groups assoc id (::data %)))
                      {::id id})
             :handle-created #(identity {:id (::id %)}))

(defresource get-group [id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :handle-ok (display-group id (@groups id)))

(defroutes routes
           (ANY "/groups" [] groups-resource)
           (ANY "/groups/:id" [id] (get-group id)))

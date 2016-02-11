(ns avalon.api.people
  (:require [liberator.core :refer [resource]]
            [compojure.core :refer [defroutes ANY]]
            [clojure.pprint :refer [pprint]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.groups :as groups]
            [avalon.models.people :as people]
            [avalon.models.games :as games]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

(def person-rules
  {:name v/required})

(defn valid-person? [kw]
  (fn [ctx]
    (let [valid (b/valid? (kw ctx) person-rules)
          errors (first (b/validate (kw ctx) person-rules))]
      [valid {::errors errors}])))

(defn gen-endpoint [id db create-fn]
  (resource :available-media-types ["application/json"]
            :allowed-methods [:post]
            :exists? (crud/exists? db id)
            :can-post-to-missing? false
            :malformed? (util/malformed? ::data)
            :processable? (valid-person? ::data)
            :handle-unprocessable-entity ::errors
            :post! (fn [ctx]
                     (let [data (::data ctx)
                           person (people/create-person (:name data))]
                       (create-fn id person)))))

(defn group-add-person [id] (gen-endpoint id groups/groups groups/add-person))
(defn game-add-person [id] (gen-endpoint id games/games games/add-person))

(defroutes routes
  (ANY "/groups/:id/people" [id] (group-add-person id))
  (ANY "/games/:id/people" [id] (game-add-person id)))

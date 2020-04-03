(ns avalon.api.people
  (:require [liberator.core :refer [defresource resource]]
            [liberator.representation :refer [ring-response]]
            [compojure.core :refer [defroutes ANY]]
            [clojure.pprint :refer [pprint]]
            [avalon.api.util :as util]
            [avalon.models.crud :as crud]
            [avalon.models.people :as people]
            [avalon.models.games :as games]
            [avalon.rules.people :as rules]
            [clojure.string :as s]))

(defn gen-endpoint [id db create-fn delete-fn]
  (resource :available-media-types ["application/json"]
            :allowed-methods [:post :delete]
            :exists? (crud/exists? db id)
            :can-post-to-missing? false
            :malformed? (util/malformed? ::data)
            :processable? (rules/valid-person? id ::data ::errors)
            :handle-unprocessable-entity ::errors
            :post! (fn [ctx]
                     (let [data (::data ctx)
                           person (people/create-person (s/trim (:name data)))]
                       (create-fn id person)
                       {::id (:id person)}))
            :handle-created #(identity {:id (::id %)})
            :respond-with-entity? true
            :delete! (fn [ctx]
                       (let [name (:name (::data ctx))]
                         (delete-fn id name)))
            :handle-ok (fn [_] (games/display-game (crud/get games/games id)))))

(defn game-people-resource [id] (gen-endpoint id games/games games/add-person games/delete-person))

(defn get-people [sees teams]
  [(into #{} (for [[person-id role] teams :when (sees role)]
               (:name (crud/get people/people person-id))))])

(defn vote-info [person-id {:keys [type people votes] :as vote}]
  (if vote
    (let [participants (set (map #(crud/get people/people %) people))]
      {:type        type
       :people      (mapv :name participants)
       :waiting     (->> participants
                         (remove #(contains? votes (:id %)))
                         (mapv :name))
       :participant (and (some #(= % person-id) people)
                         (not (contains? votes person-id)))})))

(defn get-info [game role person-id]
  (let [teams (dissoc (:teams game) person-id)
        evil-roles #{:morgana :mordred :assassin :bad :evil-lancelot1}
        evil (get-people evil-roles teams)
        evil-lancelot2 (concat evil (get-people #{:evil-lancelot2} teams))]
    (condp = role
      :merlin (get-people #{:morgana :bad :assassin :oberon :evil-lancelot1 :evil-lancelot2} teams)
      :percival (get-people #{:morgana :merlin} teams)
      :twin1 (get-people #{:twin2} teams)
      :twin2 (get-people #{:twin1} teams)
      :mordred evil-lancelot2
      :morgana evil-lancelot2
      :assassin evil-lancelot2
      :bad evil-lancelot2
      :good #{}
      :oberon #{}
      :good-lancelot1 (get-people #{:evil-lancelot1} teams)
      :evil-lancelot1 (concat evil (get-people #{:good-lancelot1} teams))
      :good-lancelot2 #{}
      :evil-lancelot2 #{})))

(defn person-exists? [id person-id]
  (if-let [game (crud/get games/games id)]
    (contains? (:people game) person-id)
    false))

(defn info-response [game person-id]
  (let [role ((:teams game) person-id)]
    {:role  role
     :first (:name (crud/get people/people (:first game)))
     :info  (get-info game role person-id)
     :vote  (vote-info person-id (:vote game))}))

(defresource person-info-resource [id person-id]
             :available-media-types ["application/json"]
             :allowed-methods [:get]
             :exists? (person-exists? id person-id)
             :handle-ok (fn [_]
                          (let [game (crud/get games/games id)
                                [valid? errors] (rules/valid-info? game)]
                            (if-not valid?
                              (ring-response errors {:status 422})
                              (info-response game person-id)))))

(defresource person-vote-resource [id person-id]
             :available-media-types ["application/json"]
             :allowed-methods [:post]
             :exists? (person-exists? id person-id)
             :can-post-to-missing? false
             :malformed? (util/malformed? ::data)
             :new? false
             :processable? #(rules/valid-vote? id person-id (::data %) ::errors)
             :handle-unprocessable-entity ::errors
             :post! (fn [ctx]
                      (let [choice (-> ctx ::data :choice keyword)]
                        (games/update-vote id person-id choice)))
             :respond-with-entity? true
             :handle-ok (fn [_] (info-response (crud/get games/games id) person-id)))

(defroutes routes
           (ANY "/games/:id/people" [id] (game-people-resource (.toLowerCase id)))
           (ANY "/games/:id/people/:person-id/info" [id person-id] (person-info-resource (.toLowerCase id) (.toLowerCase person-id)))
           (ANY "/games/:id/people/:person-id/vote" [id person-id] (person-vote-resource (.toLowerCase id) (.toLowerCase person-id))))

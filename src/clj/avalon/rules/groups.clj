(ns avalon.rules.groups
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(def join-group-rules
  {:name v/required
   :code v/required})

(defn valid-group? [req key]
  (let [valid (b/valid? req join-group-rules)
        errors (first (b/validate req join-group-rules))]
    [valid {key errors}]))

(ns avalon.rules.people
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(def person-rules
  {:name v/required})

(defn valid-person? [kw key]
  (fn [ctx]
    (let [valid (b/valid? (kw ctx) person-rules)
          errors (first (b/validate (kw ctx) person-rules))]
      [valid {key errors}])))

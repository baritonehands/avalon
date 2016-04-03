(ns avalon.utils
  (:require [reagent.session :as session]))

(defn row [& children]
  [:div.row children])

(defn col [& children]                                      ; {:keys [width m-width] :or {width 4 m-width 12}}]
  [:div.col-xs-12.col-sm-4 children])

(defn capitalize [s]
  (let [letter (-> s
                   (.charAt name 0)
                   (.toUpperCase))]
    (str letter (.slice s 1))))

(defn show-error [title msg]
  (session/put! :error {:title title :message msg}))

(ns avalon.play.quests
  (:require [reagent.session :as session]))

(defn state []
  (session/get ::state))

(defn open-dialog [quest]
  (session/put! ::state quest))

(defn close-dialog []
  (session/remove! ::state))

(defn valid? []
  (let []))

(ns avalon.utils
  (:require [reagent.session :as session]
            [material-ui :as ui]))

(defn row [& children]
  (into [:div.row] children))

(defn col [props & children]
  (let [defaults {:item true :xs 12 :sm 4}]
    (if (map? props)
      (into [:> ui/Grid (merge defaults props)] children)
      (into [:> ui/Grid defaults] (cons props children)))))

(defn spinner []
  [:> ui/Grid {:container true
               :justify   "center"}
   [:> ui/CircularProgress {:color "secondary"}]])

(defn capitalize [s]
  (let [letter (-> s
                   (.charAt name 0)
                   (.toUpperCase))]
    (str letter (.slice s 1))))

(defn show-error [title msg]
  (session/put! :error {:title title :message msg}))

(defn make-styles [f]
  (let [mk-fn (ui/makeStyles
                (fn [theme]
                  (clj->js (f theme))))]
    (fn [& args]
      (js->clj (apply mk-fn args) :keywordize-keys true))))

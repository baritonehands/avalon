(ns avalon.utils
  (:require [reagent.session :as session]
            [material-ui :as ui]
            [reagent.core :as r]))

(defn row [& children]
  (into [:div.row] children))

(defn col [props & children]
  (let [defaults {:item true :xs 12}]
    (if (map? props)
      (into [:> ui/Grid (merge defaults props)] children)
      (into [:> ui/Grid defaults] (cons props children)))))

(defn spinner []
  [:> ui/Grid {:container true
               :justify   "center"}
   [:> ui/CircularProgress {:color "secondary"}]])

(defn subheader-element [props & children]
  (let [defaults {:disable-sticky true}]
    (r/as-element
      (if (map? props)
        (into [:> ui/ListSubheader (merge defaults props)] children)
        (into [:> ui/ListSubheader defaults] (cons props children))))))

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

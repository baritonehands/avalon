(ns avalon.util)

(defn set-prop [state key]
  (fn [event]
    (swap! state assoc key (.-target.value event))))

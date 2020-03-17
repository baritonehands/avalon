(ns avalon.repl
  (:require [figwheel.main.api :as fig]
            [avalon.handler :as handler]
            [ring.server.standalone :as ring]))
;; This namespace is loaded automatically by nREPL

(defn start-figwheel
  "Start figwheel for electron main build"
  []
  (fig/start "app"))

(defn stop-figwheel
  "Stops figwheel"
  []
  (fig/stop-all))

(defonce server (atom nil))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (ring/serve
              #'handler/app
              {:port         port
               :auto-reload? true
               :join?        false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

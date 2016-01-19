(ns avalon.handler
  (:require [compojure.core :refer [GET defroutes context]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [avalon.api.groups]
            [avalon.api.games]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(def loading-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (if (env :dev)
       (include-css "vendor/material-ui/material.css" "css/site.css")
       (include-css "css/site.min.css"))]
    [:body
     mount-target
     (include-js "vendor/material-ui/material.js" "vendor/material-ui/add-robo.js" "js/app.js")]]))


(defroutes routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)

  (context "/api" []
    avalon.api.groups/routes
    avalon.api.games/routes)
  
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-params #'routes)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))

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
            [avalon.api.games]
            [avalon.api.people]))

(def mount-target
  [:div#app
      [:h3.text-center "Loading..."]])

(def loading-page
  (html
   [:html
    [:head
     [:base {:href "/"}]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:title "Avalon"]
     ;(include-css "https://fonts.googleapis.com/icon?family=Material+Icons")
     (if (env :dev)
       (include-css "css/site.css" "css/app.css")
       (include-css "css/site.min.css"))]
    [:body
     mount-target
     (include-js "vendor/material-ui/material.min.js" "vendor/material-ui/add-robo.js" "js/app.js")
     [:script """(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                                (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
               m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-56097124-2', 'auto');
ga('set', 'page', 'home-page');
ga('send', 'pageview');"""]]]))


(defroutes routes
  (GET "/" [] loading-page)
  (GET "/games/:id/play/:person-id" [_ _] loading-page)
  (GET "/groups" [] loading-page)
  (GET "/about" [] loading-page)

  (context "/api" []
    avalon.api.groups/routes
    avalon.api.games/routes
    avalon.api.people/routes)
  
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-params #'routes)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))

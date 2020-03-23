(ns avalon.handler
  (:require [compojure.core :refer [GET defroutes context]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [environ.core :refer [env]]
            [avalon.api.games]
            [avalon.api.people]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(def version
  (json/read-str (slurp (io/resource "version.json"))))

(defn prodify [s]
  (if (env :dev)
    s
    (str s "?v=" (get version "version"))))

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
     (include-css "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap")
     (include-css "https://fonts.googleapis.com/icon?family=Material+Icons")]
    [:body
     mount-target
     (include-js (prodify "js/app.js"))
     [:script """(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                                (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
               m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-56097124-2', 'auto');
ga('set', 'page', 'home-page');
ga('send', 'pageview');"""]
     [:script "avalon.core.init();"]]]))


(defroutes routes
  (GET "/" [] loading-page)
  (GET "/games/:id/play/:person-id" [_ _] loading-page)
  (GET "/about" [] loading-page)

  (context "/api" []
    avalon.api.games/routes
    avalon.api.people/routes)

  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-params #'routes)]
    (if (env :dev)
      (-> handler wrap-exceptions wrap-reload)
      (-> handler wrap-gzip))))

(def build-version (or (System/getenv "BUILD_NUMBER") "0"))
(def release-version (str "0.4." build-version))

(defproject baritonehands/avalon "0.4.0"
  :description "An Avalon web app for starting the game and keeping stats"
  :url "https://github.com/baritonehands/avalon"
  :license {:name "Apache License, v2.0"
            :url  "http://www.apache.org/licenses/"}
  :manifest {"Implementation-Version" ~release-version}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring-server "0.4.0"]
                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [ring "1.7.1"]
                 [amalloy/ring-gzip-middleware "0.1.4"]
                 [liberator "0.15.3"]
                 [org.clojure/data.json "0.2.6"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [environ "1.0.1"]

                 [org.clojure/clojurescript "1.10.597" :scope "provided"]
                 [reagent "0.10.0"]
                 [reagent-utils "0.3.3"]
                 [cljs-ajax "0.8.0"]
                 [bouncer "1.0.0"]
                 [clj-commons/secretary "1.2.4"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]
                 [cljsjs/material-ui "4.9.5-0"]
                 [cljsjs/material-ui-icons "4.4.1-0"]]

  :plugins [[lein-environ "1.0.1"]
            [lein-cljsbuild "1.1.5"]]

  :aliases {"version" ^{:doc "Generate version file for cache busting"}
                      ["run" "-m" "avalon.build/version" ~release-version]
            "fig"     ["trampoline" "run" "-m" "figwheel.main"]
            "fig:app" ["with-profile" "app" "fig" "--" "-b" "app" "-r"]}

  :ring {:handler      avalon.handler/app
         :uberwar-name "avalon.war"}

  :min-lein-version "2.9.1"

  :uberjar-name "avalon.jar"

  :main avalon.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc"]
                             :compiler     {:output-to     "target/cljsbuild/public/js/app.js"
                                            :output-dir    "target/cljsbuild/public/js/out"
                                            :asset-path    "js/out"
                                            :optimizations :none
                                            :pretty-print  true}}}}

  :profiles {:dev     {:repl-options {:init-ns avalon.repl}

                       :dependencies [[ring/ring-mock "0.3.0"]
                                      [ring/ring-devel "1.7.1"]
                                      [com.bhauman/figwheel-main "0.2.3"]
                                      [cider/piggieback "0.4.1"]
                                      [pjstadig/humane-test-output "0.7.0"]
                                      [javax.xml.bind/jaxb-api "2.3.0"]
                                      [com.sun.xml.bind/jaxb-core "2.3.0"]
                                      [com.sun.xml.bind/jaxb-impl "2.3.0"]]


                       :source-paths ["env/dev/clj"]}

             :app     {:dependencies [[com.bhauman/figwheel-main "0.2.3"]
                                      [cider/piggieback "0.4.1"]]
                       :source-paths ["env/dev/clj" "src/cljc" "src/cljs"]
                       :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]
                                      :init-ns          avalon.repl
                                      :port             7888}
                       :env          {:dev true}}
             :uberjar {:prep-tasks  ["compile" ["cljsbuild" "once"]]
                       :env         {:production true}
                       :aot         :all
                       :omit-source true
                       :cljsbuild   {:jar    true
                                     :builds {:app {:compiler {:optimizations :advanced
                                                               :pseudo-names  false
                                                               :externs       ["material-ui-externs.js"]
                                                               :main          avalon.core}}}}}})

(def build-version (or (System/getenv "BUILD_NUMBER") "0"))
(def release-version (str "0.1." build-version))

(defproject avalon "0.1.0"
  :description "An Avalon web app for starting the game and keeping stats"
  :url "https://github.com/baritonehands/avalon"
  :license {:name "Apache License, v2.0"
            :url  "http://www.apache.org/licenses/"}
  :manifest {"Implementation-Version" ~release-version}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring-server "0.4.0"]
                 [reagent "0.5.1"
                  :exclusions [org.clojure/tools.reader
                               cljsjs/react]]
                 [reagent-forms "0.5.13"]
                 [reagent-utils "0.1.5"]
                 [cljs-ajax "0.5.3"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [liberator "0.14.0"]
                 [org.clojure/data.json "0.2.6"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [environ "1.0.1"]
                 [org.clojure/clojurescript "1.7.170" :scope "provided"]
                 [bouncer "1.0.0"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.5"
                  :exclusions [org.clojure/tools.reader]]

                 ]

  :plugins [[lein-environ "1.0.1"]
            [lein-cljsbuild "1.1.1"]
            [lein-asset-minifier "0.2.2"
             :exclusions [org.clojure/clojure]]]

  :aliases {"version" ^{:doc "Generate version file for cache busting"}
                      ["run" "-m" "avalon.build/version" ~release-version]}

  :ring {:handler      avalon.handler/app
         :uberwar-name "avalon.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "avalon.jar"

  :main avalon.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" ["resources/public/css/site.css"
                                         "resources/public/css/app.css"]}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc"]
                             :compiler     {:preamble      ["resources/public/vendor/material-ui/material.min.js"]
                                            :output-to     "target/cljsbuild/public/js/app.js"
                                            :output-dir    "target/cljsbuild/public/js/out"
                                            :asset-path    "js/out"
                                            :optimizations :none
                                            :pretty-print  true}}}}

  :profiles {:dev     {:repl-options {:init-ns avalon.repl}

                       :dependencies [[ring/ring-mock "0.3.0"]
                                      [ring/ring-devel "1.4.0"]
                                      [lein-figwheel "0.5.0-2"
                                       :exclusions [org.clojure/core.memoize
                                                    ring/ring-core
                                                    org.clojure/clojure
                                                    org.ow2.asm/asm-all
                                                    org.clojure/data.priority-map
                                                    org.clojure/tools.reader
                                                    org.clojure/clojurescript
                                                    org.clojure/core.async
                                                    org.clojure/tools.analyzer.jvm]]
                                      [org.clojure/clojurescript "1.7.170"
                                       :exclusions [org.clojure/clojure org.clojure/tools.reader]]
                                      [org.clojure/tools.nrepl "0.2.12"]
                                      [com.cemerick/piggieback "0.2.1"]
                                      [pjstadig/humane-test-output "0.7.0"]
                                      ]

                       :source-paths ["env/dev/clj"]
                       :plugins      [[lein-figwheel "0.5.0-2"
                                       :exclusions [org.clojure/core.memoize
                                                    ring/ring-core
                                                    org.clojure/clojure
                                                    org.ow2.asm/asm-all
                                                    org.clojure/data.priority-map
                                                    org.clojure/tools.reader
                                                    org.clojure/clojurescript
                                                    org.clojure/core.async
                                                    org.clojure/tools.analyzer.jvm]]
                                      [org.clojure/clojurescript "1.7.170"]

                                      [com.cemerick/clojurescript.test "0.3.3"]]

                       :injections   [(require 'pjstadig.humane-test-output)
                                      (pjstadig.humane-test-output/activate!)]

                       :figwheel     {:http-server-root "public"
                                      :server-port      3449
                                      :nrepl-port       7002
                                      :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                                                         ]
                                      :css-dirs         ["resources/public/css"]
                                      :ring-handler     avalon.handler/app}

                       :env          {:dev true}

                       :cljsbuild    {:builds        {:app  {:source-paths ["env/dev/cljs"]
                                                             :compiler     {:main       "avalon.dev"
                                                                            :source-map true}}
                                                      :test {:source-paths ["src/cljs" "src/cljc" "test/cljs"]
                                                             :compiler     {:output-to     "target/test.js"
                                                                            :optimizations :whitespace
                                                                            :pretty-print  true}}

                                                      }
                                      :test-commands {"unit" ["phantomjs" :runner
                                                              "test/vendor/es5-shim.js"
                                                              "test/vendor/es5-sham.js"
                                                              "test/vendor/console-polyfill.js"
                                                              "target/test.js"]}
                                      }}

             :uberjar {:hooks       [minify-assets.plugin/hooks]
                       :prep-tasks  ["compile" ["cljsbuild" "once"]]
                       :env         {:production true}
                       :aot         :all
                       :omit-source true
                       :cljsbuild   {:jar    true
                                     :builds {:app
                                              {:source-paths ["env/prod/cljs"]
                                               :compiler
                                                             {:optimizations :advanced
                                                              :pretty-print  false}}}}}})

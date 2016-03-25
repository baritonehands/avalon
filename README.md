# Avalon

An avalon web app for starting the game and keeping stats, built in Clojure, ClojureScript, and Reagent.

## Installation

Requires [Leiningen](http://leiningen.org/) on Java 1.7+. Follow the README for the [reagent-template](https://github.com/reagent-project/reagent-template) project for more detail.

For development, just run:

    lein figwheel

In IntelliJ w/Cursive, you can connect using the REPL run configuration. Once lein is started, attach using
the REPL option.


    (fill-test-data 9)

That will create a game and fill with 9 players. Then you can copy the id and join in the browser.
Start the game there and you can see individual roles with:

    (play-roles "<id>")

That will generate the url to see each player's information.

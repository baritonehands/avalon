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

## Randomness Analysis

There's a special main function to analyse randomness:

    lein run -m subtle-bias N
    
Where N is the number of iterations to try (default 1000). Larger than 10,000 may take a long time.

Result is [bool bool], where it indicates if the first and last players are good and bad respectively.

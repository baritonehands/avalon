(ns avalon.rules.quests)

(def counts
  {5  [2 3 2 3 3]
   6  [2 3 4 3 4]
   7  [2 3 3 4 4]
   8  [3 4 4 5 5]
   9  [3 4 4 5 5]
   10 [3 4 4 5 5]})

(defn failed? [player-count n {:keys [success failure]
                               :or   {success 0
                                      failure 0}}]
  (if (and (contains? #{8 9 10} player-count)
           (= n 3))
    (>= failure 2)
    (>= failure 1)))

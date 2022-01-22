(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]))

;;generators

; structure matching
;; USE GRAPH API ;;

; where matching


; match

(defn match-query [graph query & [only-ways]]
  (let [ways (get-matched-ways (graph :adjacency) query)]
    (if (boolean only-ways) ways
      [ways (map #(merge (select-keys (graph :adjacency) %)) ways)])))
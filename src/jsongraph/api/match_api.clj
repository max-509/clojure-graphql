(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]
            [jsongraph.impl.query.where :refer [where-filter]]
            [jsongraph.api.graph-api :refer [create-graph]]))

;;generators

; structure matching
;; USE GRAPH API ;;

; where matching


; match

(defn match-query [graph query & [only-ways]]
  (let [ways (get-matched-ways (graph :adjacency) query)]
    (if (boolean only-ways) ways
      [ways (map #(merge (select-keys (graph :adjacency) %)) ways)])))

(defn match [graph nodes edges where-expr-tree]
  (let [value-extracter (fn [var] (:var-value var))
        query-graph (create-graph (map value-extracter nodes) (map value-extracter edges))
        [ways founded-patterns] (match-query graph (:adjacency query-graph))]
    (where-filter ways founded-patterns nodes edges where-expr-tree)))
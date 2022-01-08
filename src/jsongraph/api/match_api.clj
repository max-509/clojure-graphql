(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]
            [jsongraph.api.graph-api :refer [create-graph]]))

;;generators

; structure matching
;; USE GRAPH API ;;

; where matching


; match

(defn match-query
  ([graph nodes edges where-expr-tree]
   (let [value-extracter (fn [var] (:var-value var))
         query-graph (create-graph (map value-extracter nodes) (map value-extracter edges))]
     (match-query graph (:adjacency query-graph) where-expr-tree)))
  ([graph query-graph] (match-query graph query-graph `()))
  ([graph query-graph where-expr-tree]
   (clojure.pprint/pprint "graph")
   (clojure.pprint/pprint (:adjacency graph))
   (clojure.pprint/pprint "query")
   (clojure.pprint/pprint query-graph)
   (map
     #(merge (select-keys (graph :adjacency) %))
     (get-matched-ways (graph :adjacency) query-graph))))
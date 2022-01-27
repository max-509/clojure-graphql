(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]
            [jsongraph.impl.query.where :refer [where-filter]]
            [jsongraph.api.graph-api :refer :all]))

;;generators

; structure matching
;; USE GRAPH API ;;

; match

(defn match-query [graph query & [only-ways]]
  (let [ways (get-matched-ways graph query)]
    (if (boolean only-ways) ways
                            [ways (map #(merge (select-keys (graph :adjacency) %)) ways)])))

(defn- subgraphs2nodes-edges-groups [founded-subgraphs edges]
  (mapv (fn [founded-subgraph]
          (let [founded-nodes founded-subgraph
                founded-edges (apply merge
                                     (map (fn [edge-var]
                                            (let [edge-var-name (:var-name edge-var)
                                                  edge-var-value (:var-value edge-var)
                                                  source-node (founded-subgraph (edge-source edge-var-value))
                                                  target-node (founded-subgraph (edge-target edge-var-value))
                                                  edge-data (get (:out-edges (get source-node (index source-node)))
                                                                 (index target-node))
                                                  labels (:labels edge-data)
                                                  properties (:properties edge-data)]
                                              {edge-var-name (gen-edge-data source-node target-node
                                                                            labels properties)}))
                                          edges))]
            [founded-nodes founded-edges]))
        founded-subgraphs))

(defn match [graph nodes edges where-expr-tree]
  (let [value-extracter (fn [var] (:var-value var))
        query-graph (create-graph (map value-extracter nodes) (map value-extracter edges))
        founded-subgraphs (ullmann-match (:adjacency graph) (:adjacency query-graph))
        nodes-edges-groups (subgraphs2nodes-edges-groups founded-subgraphs edges)]
    (where-filter nodes-edges-groups where-expr-tree)))
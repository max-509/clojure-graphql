(ns clojure-graphql.impl.query-processing.link-processing
  (:require [clojure-graphql.impl.variables-utils :as vutils]

            [jsongraph.api.graph-api :as jgraph]))

(defn- multiply-link-entities2variables [mult-link-entities]
  (map (fn [[var-name link-entities]]
         (vutils/create-variable var-name link-entities))
       (seq mult-link-entities)))

(defn link-processing [link-nodes link-edges founded-nodes-by-vars]
  (let [founded-nodes-by-vars (into {} (map (fn [founded-node-var]
                                              {(vutils/get-var-name founded-node-var)
                                               (vutils/get-var-value founded-node-var)})
                                            founded-nodes-by-vars))
        n-nodes (count (first (vals founded-nodes-by-vars)))
        multiply-link-nodes (apply merge (map (fn [link-node]
                                                (let [var-name (vutils/get-var-name link-node)
                                                      var-value (vutils/get-var-value link-node)
                                                      founded-nodes (founded-nodes-by-vars var-name)]
                                                  (if (nil? founded-nodes)
                                                    {var-name (repeat n-nodes var-value)}
                                                    {var-name founded-nodes})))
                                              link-nodes))
        multiply-link-edges (apply merge (map (fn [link-edge]
                                                (let [var-name (vutils/get-var-name link-edge)
                                                      var-value (vutils/get-var-value link-edge)
                                                      source-nodes (multiply-link-nodes (jgraph/edge-source var-value))
                                                      target-nodes (multiply-link-nodes (jgraph/edge-target var-value))
                                                      labels (jgraph/edge-labels var-value)
                                                      properties (jgraph/edge-properties var-value)]
                                                  {var-name (mapv (fn [[source-node target-node]]
                                                                    (jgraph/gen-edge-data source-node target-node
                                                                                          labels properties))
                                                                  (mapv vector source-nodes target-nodes))}))
                                              link-edges))]
    [(multiply-link-entities2variables multiply-link-nodes)
     (multiply-link-entities2variables multiply-link-edges)]))

(ns clojure-graphql.impl.variables-utils
  (:require [jsongraph.api.graph-api :as jgraph])
  (:require [clojure.string :refer [blank?]])
  (:require [clojure-graphql.impl.query-context :refer [get-qcontext-var]]))

(defn create-variable [var-name var-value]
  {:var-name var-name :var-value var-value})

(defn get-var-value [var]
  (:var-value var))

(defn get-var-name [var]
  (:var-name var))

(defn add-variables-to-context [context variables adder]
  (reduce (fn [context var]
            (let [var-name (get-var-name var)
                  var-val (get-var-value var)]
              (if (not (blank? var-name))
                (let [var-by-name (get-qcontext-var context var-name)]
                  (if (nil? var-by-name)
                    (adder context var-name [var-val])
                    context))
                context)))
          context
          variables))

(defn replace-uuid-by-variables-names [nodes-vars edges-vars]
  (let [replaced-nodes (into {} (map (fn [node-var]
                                       (let [node-var-name (get-var-name node-var)
                                             node-var-value (get-var-value node-var)
                                             old-id (jgraph/index node-var-value)
                                             new-id (if (blank? node-var-name) old-id node-var-name)
                                             labels (node-var-value :labels)
                                             properties (node-var-value :properties)]
                                         [old-id (create-variable new-id (jgraph/gen-node labels properties new-id))]))
                                     nodes-vars))
        replaced-edges (map (fn [edge-var]
                              (let [edge-var-name (get-var-name edge-var)
                                    edge-var-value (get-var-value edge-var)
                                    source-id (jgraph/edge-source edge-var-value)
                                    target-id (jgraph/edge-target edge-var-value)
                                    labels (jgraph/edge-labels edge-var-value)
                                    properties (jgraph/edge-properties edge-var-value)]
                                (create-variable (if (blank? edge-var-name)
                                                   (clj-uuid/v4)
                                                   edge-var-name)
                                                 (jgraph/gen-edge-data
                                                   (get-var-value (get replaced-nodes source-id))
                                                   (get-var-value (get replaced-nodes target-id))
                                                   labels
                                                   properties))))
                            edges-vars)]
    [(vals replaced-nodes) replaced-edges]))

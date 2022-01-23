(ns clojure-graphql.impl.variables-utils
  (:require [clojure.string :refer [blank?]]

            [clojure-graphql.impl.query-context :refer [get-qcontext-var]]
            [clojure-graphql.impl.query-context :as qcont]
            [clojure-graphql.impl.query-extracter :as qextr]

            [jsongraph.api.graph-api :as jgraph]))

(defn create-variable [var-name var-value]
  {:var-name var-name :var-value var-value})

(defn get-var-value [var]
  (:var-value var))

(defn get-var-name [var]
  (:var-name var))

;(defn- replace-edges-by-variables [edges variables]
;  (reduce (fn [replaced-edges edge]
;            (let [edge-name (get-var-name edge)
;                  edge-value (get-var-value edge)
;                  edge-source (jgraph/edge-source edge-value)
;                  edge-target (jgraph/edge-target edge-value)
;                  var-by-name (get variables edge-name)]
;              (if (nil? var-by-name)
;                (conj replaced-edges {:var-name edge-name :var-value [edge-value]})
;                (let [var-by-name-type (first var-by-name)
;                      var-by-name-values (second var-by-name)]
;                  (if (not= :edges var-by-name-type)
;                    (throw (RuntimeException. (str "Error: variable " edge-name
;                                                   " must be edge, actual type: " (name var-by-name-type))))
;                    (conj replaced-edges
;                          {:var-name  edge-name
;                           :var-value (mapv (fn [var-by-name-value]
;                                              (jgraph/gen-edge-data
;                                                edge-source
;                                                edge-target
;                                                (jgraph/edge-labels var-by-name-value)
;                                                (jgraph/edge-properties var-by-name-value)))
;                                            var-by-name-values)}))))))
;          []
;          edges))

(defn- replace-edges-by-nodes [node-index var-nodes var-edges]
  (let [var-nodes-indexes (map (fn [var-node] (first (keys var-node))) var-nodes)]
    (mapv
      (fn [var-edge]
        (let [var-edge-name (get-var-name var-edge)
              var-edge-value (get-var-value var-edge)]
          {:var-name  var-edge-name
           :var-value (apply concat
                             (mapv (fn [edge]
                                     (let [edge-source (jgraph/edge-source edge)
                                           edge-target (jgraph/edge-target edge)
                                           edge-labels (jgraph/edge-labels edge)
                                           edge-properties (jgraph/edge-properties edge)]
                                       (cond
                                         (= node-index edge-source) (mapv (fn [var-node-idx]
                                                                            (jgraph/gen-edge-data var-node-idx edge-target
                                                                                                  edge-labels edge-properties))
                                                                          (filter #(not= % edge-target) var-nodes-indexes))
                                         (= node-index edge-target) (mapv (fn [var-node-idx]
                                                                            (jgraph/gen-edge-data edge-source var-node-idx
                                                                                                  edge-labels edge-properties))
                                                                          (filter #(not= edge-source %) var-nodes-indexes))
                                         :default [edge])))
                                   var-edge-value))}))
      var-edges)))

(defn- filter-nodes-by-variables [nodes edges variables]
  (reduce (fn [[filtered-nodes replaced-edges] node]
            (let [node-name (get-var-name node)
                  node-value (get-var-value node)
                  node-index (jgraph/index node-value)
                  var-by-name (get variables node-name)]
              (if (nil? var-by-name)
                [(cons node filtered-nodes) replaced-edges]
                (let [var-by-name-type (first var-by-name)
                      var-by-name-values (second var-by-name)]
                  (if (not= :nodes var-by-name-type)
                    (throw (RuntimeException. (str "Error: variable " node-name
                                                   " must be node, actual type: " (name var-by-name-type))))
                    [filtered-nodes (replace-edges-by-nodes node-index var-by-name-values replaced-edges)])))))
          [[] edges]
          nodes))

(defn replace-nodes-edges-by-variables [nodes edges variables]
  (let [[filtered-nodes replaced-edges] (filter-nodes-by-variables nodes edges variables)]
    [filtered-nodes replaced-edges]))

(defn add-variables-to-context [context variables adder]
  (reduce (fn [context var]
            (let [var-name (get-var-name var)
                  var-val (get-var-value var)]
              (if (not (blank? var-name))
                (adder context var-name var-val)
                context)))
          context
          variables))

(defn delete-by-vars [context variables]
  (reduce (fn [acc-context variable]
            (let [var-name (qextr/extract-variable-name-data variable)
                  var (qcont/get-qcontext-var acc-context var-name)
                  var-val (qcont/get-qcontext-var-val var)
                  old-graph (qcont/get-qcontext-graph acc-context)
                  new-graph (cond
                              (qcont/qcontext-var-nodes? var) (jgraph/delete-nodes old-graph var-val)
                              (qcont/qcontext-var-edges? var) (jgraph/delete-edges old-graph var-val)
                              :default old-graph)
                  acc-context (->
                                (qcont/set-qcontext-graph acc-context new-graph)
                                (qcont/delete-qcontext-var var-name))]
              acc-context))
          context
          variables))

(defn replace-uuid-by-variables-names [nodes-vars edges-vars]
  (let [replaced-nodes (into {} (map (fn [node-var]
                                       (let [node-var-name (get-var-name node-var)
                                             node-var-value (get-var-value node-var)
                                             old-id (jgraph/index node-var-value)
                                             new-id (if (blank? node-var-name) old-id node-var-name)
                                             labels (jgraph/node-labels (jgraph/node-val node-var-value))
                                             properties (jgraph/node-properties (jgraph/node-val node-var-value))]
                                         [old-id (create-variable new-id (jgraph/gen-node labels properties new-id))]))
                                     nodes-vars))
        replaced-edges (map (fn [edge-var]
                              (let [edge-var-name (get-var-name edge-var)
                                    [edge-var-value] (get-var-value edge-var)
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

(defn filter-pattern-by-var [pattern]
  (filter (fn [var-node]
            (not (uuid? (first var-node))))
          (seq pattern)))

(defn filter-founded-patterns-by-var [founded-patterns]
  (reduce (fn [[nodes edges] pattern]
            (let [var-nodes (filter-pattern-by-var (first pattern))
                  var-edges (filter-pattern-by-var (second pattern))]
              [(concat nodes var-nodes) (concat edges var-edges)]))
          [[] []]
          founded-patterns))

(defn reduce-founded-patterns-by-vars [founded-patterns]
  (mapv (fn [var]
          {:var-name (first var) :var-value (second var)})
        (seq (reduce (fn [founded-patterns-by-vars pattern]
                       (merge-with into
                                   founded-patterns-by-vars
                                   {(first pattern) (second pattern)}))
                     {}
                     founded-patterns))))

(defn- get-labels-properties-from-nodes [nodes]
  (mapv
    (fn [node]
      (let [node-val (jgraph/node-val node)]
        {:labels     (jgraph/node-labels node-val)
         :properties (jgraph/node-properties node-val)}))
    nodes))

(defn- get-labels-properties-from-edges [edges]
  (mapv
    (fn [edge-val]
      {:labels     (jgraph/edge-labels edge-val)
       :properties (jgraph/edge-properties edge-val)})
    edges))

(defn get-labels-properties-from-vars [vars]
  (into {} (map (fn [var]
                  (let [var-name (first var)
                        var-val (second var)
                        val-type (first var-val)
                        values (second var-val)]
                    [var-name
                     [val-type
                      (cond
                        (= :nodes val-type) (get-labels-properties-from-nodes values)
                        (= :edges val-type) (get-labels-properties-from-edges values)
                        :default (throw (RuntimeException. (str "Error: Not supported graph type" (name val-type)))))]]))
                (seq vars))))

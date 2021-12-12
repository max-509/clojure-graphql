(ns clojure-graphql.impl.memory-db-impl
  (:require [jsongraph.graph :as graph-mem]
            [jsongraph.utils :as graph-mem-utils])
  (:import (java.util UUID)))

(use '[clojure.pprint :only (pprint)])

(defmulti parse-prop-value (fn [val] (first val)))
(defmethod parse-prop-value :integer [val] (Integer/parseInt (second val)))
(defmethod parse-prop-value :float [val] (Double/parseDouble (second val)))
(defmethod parse-prop-value :boolean [val] (Boolean/parseBoolean (second val)))
(defmethod parse-prop-value :string [val] (second val))
(defmethod parse-prop-value :list [val]
  (let [list-values (rest val)]
    (if (empty? list-values)
      []
      (mapv #(parse-prop-value %) list-values))))

(defn variable-name-processing [var-name]
  (pprint "var-name")
  (pprint var-name)
  (if (empty? var-name)
    nil
    (first var-name)))

(defn labels-processing [labels]
  (pprint "labels")
  (pprint labels)
  (map (fn [label] (second label)) labels))

(defn properties-processing [properties params]
  (pprint "properties")
  (pprint properties)
  (into (hash-map) (map
                     (fn [prop]
                       (let [name (second (nth prop 1))
                             val (nth prop 2)]
                         [name (parse-prop-value val)]))
                     properties)))

(defn node-processing [node params]
  (pprint "node")
  (pprint node)
  (let [node-id (UUID/randomUUID)
        var-name (variable-name-processing (rest (nth node 0)))
        labels (labels-processing (rest (nth node 1)))
        properties (properties-processing (rest (nth node 2)) params)]
    (graph-mem/gen-node node-id var-name labels properties)))

(defn edge-processing [prev-node edge next-node params]
  (pprint "prev-node")
  (pprint prev-node)
  (pprint "edge")
  (pprint edge)
  (pprint "next-node")
  (pprint next-node)
  (let [left-dash (first (nth edge 0))
        relation (rest (nth edge 1))
        right-dash (first (nth edge 2))
        relation-data {:labels     (labels-processing (rest (nth relation 0)))
                       :properties (properties-processing (rest (nth relation 1)) params)}
        source-target (cond
                        (and (= left-dash :dash) (= right-dash :right-arrow)) [(key (first prev-node))
                                                                               (key (first next-node))]
                        (and (= left-dash :left-arrow) (= right-dash :dash)) [(key (first next-node))
                                                                              (key (first prev-node))])]
    (graph-mem/gen-edge source-target relation-data)))

(defn edge-node-processing [prev-node rest-edges-nodes params graph]
  (pprint "rest-edges-nodes")
  (pprint rest-edges-nodes)
  (reduce (fn [[prev acc-graph] edge-node]
            (let [next (node-processing (rest (second edge-node)) params)
                  graph-with-next (graph-mem/add-node acc-graph next)
                  processed-edge (edge-processing prev (rest (first rest-edges-nodes)) next params)
                  new-graph (graph-mem/add-edge graph-with-next [processed-edge])]
              [next new-graph]))
          [prev-node graph]
          (partition 2 rest-edges-nodes)))

(defn pattern-processing [pattern params graph]
  (pprint "pattern")
  (pprint pattern)
  (let [node (first pattern)
        rest-edges-nodes (rest pattern)
        processed-node (node-processing (rest node) params)
        new-graph (graph-mem/add-node graph processed-node)]
    (if (empty? rest-edges-nodes)
      new-graph
      (edge-node-processing processed-node rest-edges-nodes params new-graph))))

(defn patterns-processing
  [patterns params]
  (pprint "patterns")
  (pprint patterns)
  (reduce (fn [graph pattern]
            (pattern-processing (rest pattern) params graph))
          (graph-mem/gen-empty-graph)
          patterns))

(defn create-processing [session patterns context]
  (pprint "create")
  (pprint patterns)
  (let [last-version (last @session)
        new-graph (patterns-processing (rest patterns) context)]
    (pprint new-graph)))

(defn runner [session query params]
  (pprint "query")
  (pprint query)
  (reduce (fn [context clause]
            (let [clause-data (second clause)
                  clause-name (first clause-data)
                  clause-params (second clause-data)]
              (cond
                (= clause-name :create) (create-processing session clause-params context))))
          {:return-val nil
           :params     params
           :var        {}}
          (rest query)))

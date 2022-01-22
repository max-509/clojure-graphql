(ns clojure-graphql.impl.query-processing.pattern-processing
  (:require [clojure-graphql.impl.query-extracter :as qextr]
            [clojure-graphql.impl.query-context :as qcont]
            [clojure-graphql.impl.lang2cloj :as l2cloj]
            [clojure-graphql.impl.variables-utils :as vutils]

            [jsongraph.api.graph-api :as jgraph]))

(defn labels-processing [labels-data]
  (let [labels-vect (mapv (fn [label] (keyword (qextr/extract-label-data label))) labels-data)]
    (if (empty? labels-vect)
      nil
      labels-vect)))

(defn properties-processing [properties-data context]
  (let [params (qcont/get-qcontext-params context)
        properties-data-type (qextr/extract-properties-data-type properties-data)]
    (cond
      (= :external-properties properties-data-type) (let [external-prop-name (qextr/extract-external-properties properties-data)
                                                          external-props-by-name (get params (keyword external-prop-name))]
                                                      (if (= nil external-props-by-name)
                                                        (throw (RuntimeException. (str "Error: Cannot find variable '" external-prop-name "' for properties")))
                                                        external-props-by-name))
      (= :internal-properties properties-data-type) (let [properties (qextr/extract-internal-properties properties-data)]
                                                      (into (hash-map) (map
                                                                         (fn [prop]
                                                                           (let [prop-data (qextr/extract-property-data prop)
                                                                                 name (keyword (qextr/extract-property-key-data prop-data))
                                                                                 val (qextr/extract-property-val prop-data)]
                                                                             [name (l2cloj/convert-prop-value val)]))
                                                                         properties)))
      :default nil)))

(defn node-processing [node-data context]
  (let [var-name (->
                   (qextr/extract-variable node-data)
                   (qextr/extract-variable-name-data))
        labels (labels-processing (qextr/extract-labels-data node-data))
        properties (properties-processing (qextr/extract-properties-data node-data) context)
        generated-node (vutils/create-variable var-name (jgraph/gen-node labels properties))]
    generated-node))

(defn relation-processing [prev-node relation-data next-node context]
  (let [left-dash (qextr/extract-left-dash relation-data)
        right-dash (qextr/extract-right-dash relation-data)
        edge (qextr/extract-edge relation-data)
        edge-data (qextr/extract-edge-data edge)

        var-name (->
                   (qextr/extract-variable edge-data)
                   (qextr/extract-variable-name-data))
        labels (labels-processing (qextr/extract-labels-data edge-data))
        properties (properties-processing (qextr/extract-properties-data edge-data) context)
        source-target (cond
                        (and (= left-dash :dash) (= right-dash :right-arrow)) [prev-node next-node]
                        (and (= left-dash :left-arrow) (= right-dash :dash)) [next-node prev-node])
        gen-edge-data (->
                        (conj source-target labels)
                        (conj properties))

        generated-edge (vutils/create-variable var-name (apply jgraph/gen-edge-data gen-edge-data))]
    generated-edge))

(defn edge-node-processing [prev-node rest-edges-nodes context]
  (let [nodes [prev-node]
        edges []]
    (first (reduce (fn [[[nodes edges] prev] edge-node]
                     (let [relation-data (->
                                           (qextr/extract-first-pattern-elem edge-node)
                                           (qextr/extract-relation-data))
                           next-node (->
                                       (qextr/extract-second-pattern-elem edge-node)
                                       (qextr/extract-node-data)
                                       (node-processing context))
                           edge (relation-processing (vutils/get-var-value prev) relation-data (vutils/get-var-value next-node) context)
                           nodes (conj nodes next-node)
                           edges (conj edges edge)]
                       [[nodes edges] next-node]))
                   [[nodes edges] prev-node]
                   (partition 2 rest-edges-nodes)))))

(defn pattern-processing [pattern-data context]
  (let [node (qextr/extract-first-pattern-elem pattern-data)
        rest-edges-nodes (qextr/extract-rest-pattern-elems pattern-data)
        processed-node (node-processing (qextr/extract-node-data node) context)]
    (if (empty? rest-edges-nodes)
      [[processed-node] []]
      (edge-node-processing processed-node rest-edges-nodes context))))

(defn patterns-processing
  [patterns context]
  (reduce (fn [[nodes edges] pattern]
            (let [[new-nodes new-edges] (pattern-processing (qextr/extract-pattern-data pattern) context)]
              [(concat nodes new-nodes) (concat edges new-edges)]))
          [[] []]
          patterns))

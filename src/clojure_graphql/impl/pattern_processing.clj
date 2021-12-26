(ns clojure-graphql.impl.pattern-processing
  (:require [jsongraph.api :as jgraph])
  (:require [clojure-graphql.impl.query_extracter :as qextr])
  (:require [clojure-graphql.impl.query-context :as qcont]))

(use '[clojure.pprint :only (pprint)])

(defmulti parse-prop-value (fn [val] (qextr/extract-property-val-type val)))
(defmethod parse-prop-value :integer [val] (Integer/parseInt (qextr/extract-property-val-data val)))
(defmethod parse-prop-value :float [val] (Double/parseDouble (qextr/extract-property-val-data val)))
(defmethod parse-prop-value :boolean [val] (Boolean/parseBoolean (qextr/extract-property-val-data val)))
(defmethod parse-prop-value :string [val] (qextr/extract-property-val-data val))
(defmethod parse-prop-value :list [val]
  (let [list-values (qextr/extract-property-val-data val)]
    (if (empty? list-values)
      []
      (mapv #(parse-prop-value %) list-values))))

(defn labels-processing [labels-data]
  (pprint "labels")
  (pprint labels-data)
  (map (fn [label] (qextr/extract-label-data label)) labels-data))

(defn properties-processing [properties-data context]
  (pprint "properties")
  (pprint properties-data)
  (let [params (qcont/get-qcontext-params context)]
    (into (hash-map) (map
                       (fn [prop]
                         (let [prop-data (qextr/extract-property-data prop)
                               name (qextr/extract-property-key-data prop-data)
                               val (qextr/extract-property-val prop-data)]
                           [name (parse-prop-value val)]))
                       properties-data))))

(defn node-processing [node-data context]
  (pprint "node")
  (pprint node-data)
  (let [var-name (->
                   (qextr/extract-variable node-data)
                   (qextr/extract-variable-name-data))
        labels (labels-processing (qextr/extract-labels-data node-data))
        properties (properties-processing (qextr/extract-properties-data node-data) context)
        generated-node (jgraph/gen-node labels properties)
        new-context (if (some? var-name)
                      (qcont/add-qcontext-vars context {var-name {:node generated-node}}))]
    [new-context generated-node]))

(defn relation-processing [prev-node relation-data next-node context]
  (pprint "prev-node")
  (pprint prev-node)
  (pprint "relation")
  (pprint relation-data)
  (pprint "next-node")
  (pprint next-node)
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
        generated-edge (apply jgraph/gen-edge gen-edge-data)
        new-context (if (some? var-name)
                      (qcont/add-qcontext-vars context {var-name {:edge generated-edge}}))]
    [new-context generated-edge]))

(defn edge-node-processing [prev-node rest-edges-nodes context]
  (pprint "rest-edges-nodes")
  (pprint rest-edges-nodes)
  (first
    (reduce (fn [[old-context prev] edge-node]
              (let [acc-graph (qcont/get-qcontext-graph old-context)
                    relation-data (->
                                    (qextr/extract-first-pattern-elem edge-node)
                                    (qextr/extract-relation-data))
                    [new-context next-node] (->
                                              (qextr/extract-second-pattern-elem edge-node)
                                              (qextr/extract-node-data)
                                              (node-processing old-context))
                    [new-context edge] (relation-processing prev relation-data next-node new-context)
                    new-graph (->
                                (jgraph/add-nodes acc-graph next-node)
                                (jgraph/add-edges [edge]))
                    new-context (qcont/set-qcontext-graph new-context new-graph)]
                [new-context next-node]))
            [context prev-node]
            (partition 2 rest-edges-nodes))))

(defn pattern-processing [pattern-data context]
  (pprint "pattern")
  (pprint pattern-data)
  (let [graph (qcont/get-qcontext-graph context)
        node (qextr/extract-first-pattern-elem pattern-data)
        rest-edges-nodes (qextr/extract-rest-pattern-elems pattern-data)
        [new-context processed-node] (node-processing (qextr/extract-node-data node) context)
        new-graph (jgraph/add-nodes graph [processed-node])
        new-context (qcont/set-qcontext-graph new-context new-graph)]
    (if (empty? rest-edges-nodes)
      new-context
      (edge-node-processing processed-node rest-edges-nodes new-context))))

(defn patterns-processing
  ;TODO: Impl for matching and other non-creating operations
  [patterns context operation-type]
  (pprint "patterns")
  (pprint patterns)
  (reduce (fn [graph pattern] (pattern-processing (qextr/extract-pattern-data pattern) context))
          context
          patterns))


(ns clojure-graphql.impl.variables-utils
  (:require [clojure-graphql.impl.query-context :refer [get-qcontext-var]]
            [clojure-graphql.impl.query-context :as qcont]
            [clojure-graphql.impl.query-extracter :as qextr]
            [clojure.string :refer [blank?]]
            [jsongraph.api.graph-api :as jgraph]))

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
                    (adder context var-name var-val)
                    context))
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
                              (= nil var) (throw (RuntimeException. "Error: in where clause must be operations with exists variable"))
                              (qcont/qcontext-var-nodes? var) (jgraph/delete-nodes old-graph var-val)
                              (qcont/qcontext-var-edges? var) (jgraph/delete-edges old-graph var-val)
                              :default old-graph)
                  acc-context (qcont/set-qcontext-graph acc-context new-graph)]
              acc-context))
          context
          variables))

(defn replace-uuid-by-variables-names [nodes-vars edges-vars]
  (let [replaced-nodes (into {} (map (fn [node-var]
                                       (let [node-var-name (get-var-name node-var)
                                             node-var-value (get-var-value node-var)
                                             old-id (jgraph/index node-var-value)
                                             new-id (if (blank? node-var-name) old-id node-var-name)
                                             labels (jgraph/node-labels node-var-value)
                                             properties (jgraph/node-properties node-var-value)]
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

(defn filter-pattern-by-var [pattern]
  (filter (fn [var-node]
            (not (uuid? (first var-node))))
          (seq pattern)))

(defn filter-founded-patterns-by-var [founded-patterns]
  (reduce (fn [[nodes edges] pattern]
            (let [var-nodes (filter-pattern-by-var (first pattern))
                  var-edges (filter-pattern-by-var (second pattern))]
              [(conj nodes var-nodes) (conj edges var-edges)]))
          [[] []]
          founded-patterns))

(defn reduce-founded-patterns-by-vars [founded-patterns]
  (mapv (fn [var]
          {:var-name (first var) :var-value (second var)})
        (seq (reduce (fn [founded-patterns-by-vars patterns]
                       (merge-with merge
                                   founded-patterns-by-vars
                                   (into {} patterns)))
                     {}
                     founded-patterns))))

(defn- get-labels-properties-from-nodes [nodes]
  ;(clojure.pprint/pprint "nodes")
  ;(clojure.pprint/pprint nodes)
  ;(clojure.pprint/pprint (mapv
  ;                         (fn [node]
  ;                           (let [node-val (jgraph/node-val node)]
  ;                             {:labels     (jgraph/node-labels node-val)
  ;                              :properties (jgraph/node-properties node-val)}))
  ;                         nodes))
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

(ns clojure-graphql.impl.query-processing.set-processing
  (:require [clojure-graphql.impl.query-extracter :as qextr]
            [clojure-graphql.impl.lang2cloj :as l2cloj]
            [clojure-graphql.impl.query-context :as qcont]

            [jsongraph.api.graph-api :as jgraph]))

(defmulti set-param-processing (fn [set-param-data context] (qextr/extract-set-param-data-command set-param-data)))
(defmethod set-param-processing :assign-command [set-param-data context]
  (let [set-param-params (qextr/extract-set-param-data-params set-param-data)
        var-name (qextr/extract-set-param-var-name set-param-params)
        field (qextr/extract-set-param-assign-field set-param-params)
        value (qextr/extract-set-param-assign-value set-param-params)
        type (qextr/extract-set-param-assign-value-type value)
        val (qextr/extract-set-param-assign-value-val value)]
    (cond
      (= :null type) {var-name {:labels [] :properties {field nil}}}
      (= :external-param type) {var-name {:labels [] :properties {field (get (qcont/get-qcontext-param context val) field)}}}
      :default {var-name {:labels [] :properties {field (l2cloj/convert-prop-value value)}}})))
(defmethod set-param-processing :label-command [set-param-data context]
  (let [set-param-params (qextr/extract-set-param-data-params set-param-data)
        var-name (qextr/extract-set-param-var-name set-param-params)
        labels-data (qextr/extract-set-param-labels-data set-param-params)
        labels-vect (mapv (fn [label] (qextr/extract-label-data label)) labels-data)]
    {var-name {:labels labels-vect :properties {}}}))

(defn- merge-labels-props
  [labels-props1 labels-props2]
  (->
    (assoc labels-props1 :labels (concat (:labels labels-props1) (:labels labels-props2)))
    (assoc :properties (merge (:properties labels-props1) (:properties labels-props2)))))

(defn- merge-labels-props-with-node [node labels-props]
  (let [index (jgraph/index node)
        value (get node index)]
    {index
     (merge-labels-props value labels-props)}))

(defn- merge-labels-props-with-edge [edge labels-props]
  (let [edge-source (jgraph/edge-source edge)
        edge-target (jgraph/edge-target edge)
        edge-labels-props {:labels (jgraph/edge-labels edge) :properties (jgraph/edge-properties edge)}
        merged-labels-props (merge-labels-props edge-labels-props labels-props)]
    (jgraph/gen-edge-data edge-source edge-target
                          (:labels merged-labels-props) (:properties merged-labels-props))))

(defn- replace-set-nodes-by-variables-uuids [var-by-name-values set-var-values]
  (reduce (fn [[new-set-results merged-nodes] var-by-name-value]
            [(cons
               [[(jgraph/index var-by-name-value)] set-var-values]
               new-set-results)
             (conj merged-nodes (merge-labels-props-with-node var-by-name-value set-var-values))])
          [[] []]
          var-by-name-values))

(defn- replace-set-edges-by-variables-uuids [var-by-name-values set-var-values]
  (reduce (fn [[new-set-results merged-edges] var-by-name-value]
            [(cons
               [[(jgraph/edge-source var-by-name-value)
                 (jgraph/edge-target var-by-name-value)] set-var-values]
               new-set-results)
             (conj merged-edges (merge-labels-props-with-edge var-by-name-value set-var-values))])
          [[] []]
          var-by-name-values))

(defn- replace-set-results-by-variables-uuids [set-results context]
  (reduce (fn [[new-set-results new-context] set-var]
            (let [all-variables (qcont/get-qcontext-vars new-context)
                  set-var-name (first set-var)
                  set-var-values (second set-var)
                  var-by-name (get all-variables set-var-name)]
              (if (some? var-by-name)
                (let [var-by-name-type (first var-by-name)
                      var-by-name-values (second var-by-name)]
                  (cond
                    (= :nodes var-by-name-type) (let [[set-results-for-add merged-nodes]
                                                      (replace-set-nodes-by-variables-uuids var-by-name-values set-var-values)]
                                                  [(concat new-set-results set-results-for-add)
                                                   (qcont/add-qcontext-nodes-var new-context
                                                                                 set-var-name
                                                                                 merged-nodes)])
                    (= :edges var-by-name-type) (let [[set-results-for-add merged-edges]
                                                      (replace-set-edges-by-variables-uuids var-by-name-values set-var-values)]
                                                  [(concat new-set-results set-results-for-add)
                                                   (qcont/add-qcontext-edges-var new-context
                                                                                 set-var-name
                                                                                 merged-edges)])
                    :default (throw (RuntimeException. (str "Error: bad variable type:" (name var-by-name-type))))))
                [new-set-results new-context])))
          [[] context]
          (seq set-results)))

(defn set-processing [set-params context]
  (replace-set-results-by-variables-uuids
    (apply merge-with
           merge-labels-props
           (map (fn [set-param]
                  (let [set-param-data (qextr/extract-set-param-data set-param)]
                    (set-param-processing set-param-data context)))
                set-params))
    context))

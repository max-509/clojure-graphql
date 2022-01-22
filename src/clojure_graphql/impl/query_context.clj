(ns clojure-graphql.impl.query-context
  (:require [jsongraph.api.graph-api :as jgraph])
  (:require [clojure.string :refer [blank?]]))

(use '[clojure.pprint :only (pprint)])

(defn get-qcontext [graph params]
  {:graph      graph
   :params     params
   :vars       {}
   :return-val nil})

(defn get-empty-qcontext []
  (get-qcontext (jgraph/create-graph) {}))

(defn get-qcontext-graph [qcontext]
  (get qcontext :graph))

(defn get-qcontext-params [qcontext]
  (get qcontext :params))

(defn get-qcontext-vars [qcontext]
  (get qcontext :vars))

(defn qcontext-var-nodes? [var]
  (= :nodes (first var)))

(defn qcontext-var-edges? [var]
  (= :edges (first var)))

(defn get-qcontext-var-val [var]
  (second var))

(defn get-qcontext-return [qcontext]
  (get qcontext :return))

(defn get-qcontext-param [qcontext name]
  (->
    (get-qcontext-params qcontext)
    (get name)))

(defn get-qcontext-var [qcontext name]
  (if (blank? name)
    nil
    (->
      (get-qcontext-vars qcontext)
      (get name))))

(defn add-qcontext-edges-var [qcontext var edges]
  (let [edges (if (seq? edges) edges [edges])]
    (assoc qcontext :vars (merge (get-qcontext-vars qcontext) {(keyword var) [:edges edges]}))))

(defn add-qcontext-nodes-var [qcontext var nodes]
  (let [nodes (mapv (fn [n] {(first n) (second n)}) (seq nodes))]
    (assoc qcontext :vars (merge (get-qcontext-vars qcontext) {(keyword var) [:nodes nodes]}))))

(defn set-qcontext-graph [qcontext graph]
  (assoc qcontext :graph graph))

(defn set-qcontext-return [qcontext ret-val]
  (assoc qcontext :return  ret-val))

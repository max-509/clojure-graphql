(ns clojure-graphql.impl.query-context
  (:require [clojure-graphql.impl.versions-tree :as vtree])
  (:require [jsongraph.api :as jgraph]))

;TODO: use graph instead db

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

(defn get-qcontext-return [qcontext]
  (get qcontext :return))

(defn get-qcontext-param [qcontext name]
  (->
    (get-qcontext-params qcontext)
    (get name)))

(defn get-qcontext-var [qcontext name]
  (->
    (get-qcontext-vars qcontext)
    (get name)))

(defn add-qcontext-vars [qcontext vars]
  (assoc qcontext :vars (merge (get-qcontext-vars qcontext) vars)))

(defn set-qcontext-graph [qcontext graph]
  (assoc qcontext :graph graph))

(defn set-qcontext-return [qcontext ret-val]
  (assoc qcontext :return  ret-val))

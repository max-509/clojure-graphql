(ns clojure-graphql.impl.query_processing.clauses_processing
  (:require [jsongraph.api.graph-api :as jgraph])
  (:require [clojure-graphql.impl.query_extracter :as qextr])
  (:require [clojure-graphql.impl.versions-tree :as vtree])
  (:require [clojure-graphql.impl.query_processing.pattern-processing :as patt-proc])
  (:require [clojure-graphql.impl.query_processing.where-processing :as where-proc])
  (:require [clojure-graphql.impl.query-context :as qcont]))

(use '[clojure.pprint :only (pprint)])

(defn match-processing [clause-data context]
  (pprint "match")
  (pprint clause-data)
  (let [patterns (qextr/extract-patterns clause-data)
        [new-context new-nodes new-edges] (patt-proc/patterns-processing patterns context)
        predicates (qextr/extract-predicates clause-data)
        where-expr-tree (where-proc/where-processing predicates)]
    (pprint "where-expr-tree")
    (pprint where-expr-tree)))

(defn undo-processing [context db]
  (pprint "undo")
  (let [last-version-graph (vtree/undo! db)]
    (qcont/set-qcontext-graph context last-version-graph)))

(defn delete-processing [clause-data context db]
  (pprint "delete")
  (pprint clause-data)
  (let [variables (qextr/extract-variables clause-data)
        new-context (reduce (fn [acc-context variable]
                              (let [var-name (qextr/extract-variable-name-data variable)
                                    var (qcont/get-qcontext-var context var-name)
                                    var-val (qcont/get-qcontext-var-val var)
                                    old-graph (qcont/get-qcontext-graph context)
                                    new-graph (cond
                                                (= nil var) (throw (RuntimeException. "Error: in where clause must be operations with exists variable"))
                                                (qcont/qcontext-var-nodes? var) (jgraph/delete-nodes old-graph var-val)
                                                (qcont/qcontext-var-edges? var) (jgraph/delete-edges old-graph var-val)
                                                :default old-graph)
                                    acc-context (qcont/set-qcontext-graph acc-context new-graph)]
                                acc-context))
                            context
                            variables)]
    (vtree/add-new-version! db (qcont/get-qcontext-graph new-context))
    new-context))

(defn create-processing [clause-data context db]
  (pprint "create")
  (pprint clause-data)
  (let [patterns (qextr/extract-patterns clause-data)
        [new-context new-nodes new-edges] (patt-proc/patterns-processing patterns context)
        updated-graph (->
                        (qcont/get-qcontext-graph new-context)
                        (jgraph/add-nodes new-nodes)
                        (jgraph/add-edges new-edges))
        new-context (qcont/set-qcontext-graph new-context updated-graph)]
    (vtree/add-new-version! db updated-graph)
    new-context))

(defmulti clause-processing (fn [clause context db] (qextr/extract-clause-name clause)))
(defmethod clause-processing :create [clause context db] (create-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :delete [clause context db] (delete-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :undo [clause context db] (undo-processing context db))
(defmethod clause-processing :match [clause context db] (match-processing (qextr/extract-clause-data clause) context))

(defn runner [db query params]
  (pprint "query")
  (pprint query)
  (let [last-version-graph (vtree/get-last-version db)
        context-after-query (reduce (fn [context clause] (clause-processing clause context db))
                                    (qcont/get-qcontext last-version-graph params)
                                    (qextr/extract-clauses query))]))

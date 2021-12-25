(ns clojure-graphql.impl.clauses_processing
  (:require [jsongraph.api :as jgraph])
  (:require [clojure-graphql.impl.query_extracter :as qextr])
  (:require [clojure-graphql.impl.versions-tree :as vtree])
  (:require [clojure-graphql.impl.pattern-processing :as patt-proc])
  (:require [clojure-graphql.impl.query-context :as qcont]))
(use '[clojure.pprint :only (pprint)])

(defn undo-processing [context db]
  (pprint "undo")
  (let [last-version-graph (vtree/undo! db)]
    (qcont/set-qcontext-graph context last-version-graph)))

(defn delete-processing [clause-data context]
  (pprint "delete")
  (pprint clause-data)
  (let [variables (qextr/extract-variables clause-data)]
    (reduce (fn [acc-context variable]
              (let [var-name (qextr/extract-variable-name-data variable)]
                ;TODO: delete var from graph by name
                (pprint (qcont/get-qcontext-var context var-name))
                acc-context))
            context
            variables)))

(defn create-processing [clause-data context db]
  (pprint "create")
  (pprint clause-data)
  (let [patterns (qextr/extract-patterns clause-data)
        new-context (patt-proc/patterns-processing patterns context :create)]
    (vtree/add-new-version! db (qcont/get-qcontext-graph new-context))
    new-context))

(defmulti clause-processing (fn [clause context db] (qextr/extract-clause-name clause)))
(defmethod clause-processing :create [clause context db] (create-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :delete [clause context db] (delete-processing (qextr/extract-clause-data clause) context))
(defmethod clause-processing :undo [clause context db] (undo-processing context db))
(defmethod clause-processing :match [clause context db] context)

(defn runner [db query params]
  (pprint "query")
  (pprint query)
  (let [last-version-graph (vtree/get-last-version db)
        context-after-query (reduce (fn [context clause] (clause-processing clause context db))
                                    (qcont/get-qcontext last-version-graph params)
                                    (qextr/extract-clauses query))]))

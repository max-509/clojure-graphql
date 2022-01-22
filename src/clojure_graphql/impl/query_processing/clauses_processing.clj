(ns clojure-graphql.impl.query-processing.clauses-processing
  (:require [dorothy.jvm :refer (save!)]
            [clojure.string :refer [blank?]]

            [jsongraph.api.graph-api :as jgraph]
            [jsongraph.api.match-api :as jmatch]
            [jsongraph.api.graphviz :as graphviz]

            [clojure-graphql.impl.query-extracter :as qextr]
            [clojure-graphql.impl.versions-tree :as vtree]
            [clojure-graphql.impl.query-processing.pattern-processing :as patt-proc]
            [clojure-graphql.impl.query-processing.where-processing :as where-proc]
            [clojure-graphql.impl.query-processing.return-processing :as return-proc]
            [clojure-graphql.impl.query-context :as qcont]
            [clojure-graphql.impl.variables-utils :as vutils]))

(use '[clojure.pprint :only (pprint)])

(defn match-processing [clause-data context]
  (pprint "match")
  (pprint clause-data)
  (let [patterns (qextr/extract-patterns clause-data)
        [nodes edges] (patt-proc/patterns-processing patterns context)
        [nodes edges] (vutils/replace-uuid-by-variables-names nodes edges)
        predicates (qextr/extract-predicates clause-data)
        where-expr-tree (where-proc/where-processing predicates)
        founded-patterns (jmatch/match (qcont/get-qcontext-graph context)
                                       nodes edges where-expr-tree)
        [founded-nodes founded-edges] (vutils/filter-founded-patterns-by-var founded-patterns)
        founded-nodes-by-vars (vutils/reduce-founded-patterns-by-vars founded-nodes)
        founded-edges-by-vars (vutils/reduce-founded-patterns-by-vars founded-edges)
        context (->
                  (vutils/add-variables-to-context context founded-nodes-by-vars qcont/add-qcontext-nodes-var)
                  (vutils/add-variables-to-context founded-edges-by-vars qcont/add-qcontext-edges-var))]
    context))

(defn undo-processing [context db]
  (pprint "undo")
  (let [last-version-graph (vtree/undo! db)]
    (qcont/set-qcontext-graph context last-version-graph)))

(defn return-processing [clause-data context]
  (pprint "return-data")
  (pprint clause-data)
  (let [return-params (qextr/extract-return-params clause-data)
        all-variables (vutils/get-labels-properties-from-vars (qcont/get-qcontext-vars context))]
    (qcont/set-qcontext-return context (return-proc/return-processing return-params all-variables))))

(defn saveviz-processing [pathname context]
  (pprint "pathname")
  (pprint pathname)
  (let [graph (qcont/get-qcontext-graph context)
        pathname (first pathname)
        graphviz-graph (graphviz/graph-to-graphviz graph)]
    (println graphviz-graph)
    (save! graphviz-graph pathname {:format :svg})
    context))

(defn delete-processing [clause-data context db]
  (let [variables (qextr/extract-variables clause-data)
        new-context (vutils/delete-by-vars context variables)]
    (vtree/add-new-version! db (qcont/get-qcontext-graph new-context))
    new-context))

(defn create-processing [clause-data context db]
  (let [patterns (qextr/extract-patterns clause-data)
        [new-nodes new-edges] (patt-proc/patterns-processing patterns context)
        [new-nodes new-edges] (vutils/replace-nodes-edges-by-variables new-nodes new-edges
                                                                       (qcont/get-qcontext-vars context))
        context (->
                  (vutils/add-variables-to-context context new-nodes qcont/add-qcontext-nodes-var)
                  (vutils/add-variables-to-context new-edges qcont/add-qcontext-edges-var))
        updated-graph (->
                        (qcont/get-qcontext-graph context)
                        (jgraph/add-nodes (map vutils/get-var-value new-nodes))
                        (jgraph/add-edges (apply concat (map vutils/get-var-value new-edges))))
        new-context (qcont/set-qcontext-graph context updated-graph)]
    (vtree/add-new-version! db updated-graph)
    new-context))

(defmulti clause-processing (fn [clause context db] (qextr/extract-clause-name clause)))
(defmethod clause-processing :create [clause context db] (create-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :delete [clause context db] (delete-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :undo [clause context db] (undo-processing context db))
(defmethod clause-processing :match [clause context db] (match-processing (qextr/extract-clause-data clause) context))
(defmethod clause-processing :return [clause context db] (return-processing (qextr/extract-clause-data clause) context))
(defmethod clause-processing :saveviz [clause context db] (saveviz-processing (qextr/extract-clause-data clause) context))

(defn runner [db query params]
  (let [last-version-graph (vtree/get-last-version db)
        context-after-query (reduce (fn [context clause] (clause-processing clause context db))
                                    (qcont/get-qcontext last-version-graph params)
                                    (qextr/extract-clauses query))]
    (qcont/get-qcontext-return context-after-query)))

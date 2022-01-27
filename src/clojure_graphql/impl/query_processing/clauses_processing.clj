(ns clojure-graphql.impl.query-processing.clauses-processing
  (:require [clojure.string :refer [blank? last-index-of join]]

            [jsongraph.api.graph-api :as jgraph]
            [jsongraph.api.match-api :as jmatch]
            [jsongraph.api.set-api :as jset]
            [jsongraph.api.graphviz :as jgraphviz]

            [clojure-graphql.impl.query-extracter :as qextr]
            [clojure-graphql.impl.versions-tree :as vtree]
            [clojure-graphql.impl.query-processing.pattern-processing :as patt-proc]
            [clojure-graphql.impl.query-processing.where-processing :as where-proc]
            [clojure-graphql.impl.query-processing.return-processing :as return-proc]
            [clojure-graphql.impl.query-processing.set-processing :as set-proc]
            [clojure-graphql.impl.query-processing.link-processing :as link-proc]
            [clojure-graphql.impl.query-context :as qcont]
            [clojure-graphql.impl.variables-utils :as vutils]))

(defn match-processing [clause-data context db]
  (let [patterns (qextr/extract-patterns clause-data)
        [nodes-patterns edges-patterns] (patt-proc/patterns-processing patterns context)
        [nodes edges] [(apply concat nodes-patterns) (apply concat edges-patterns)]
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
                  (vutils/add-variables-to-context founded-edges-by-vars qcont/add-qcontext-edges-var))

        link-params (qextr/extract-link-params clause-data)]
    (if (some? link-params)
      (let [link-patterns (qextr/extract-patterns link-params)

            [link-nodes-patterns link-edges-patterns] (patt-proc/patterns-processing link-patterns context)
            context (reduce (fn [context [link-nodes link-edges]]
                              (let [[link-nodes link-edges] (vutils/replace-uuid-by-variables-names link-nodes link-edges)
                                    [link-nodes link-edges] (link-proc/link-processing link-nodes link-edges founded-nodes-by-vars)

                                    context (->
                                              (vutils/add-variables-to-context context link-nodes qcont/add-qcontext-nodes-var)
                                              (vutils/add-variables-to-context link-edges qcont/add-qcontext-edges-var))

                                    updated-graph (->
                                                    (qcont/get-qcontext-graph context)
                                                    (jgraph/add-nodes (apply concat (map vutils/get-var-value link-nodes)))
                                                    (jgraph/add-edges (apply concat (map vutils/get-var-value link-edges))))

                                    context (qcont/set-qcontext-graph context updated-graph)]
                                context))
                            context
                            (map vector link-nodes-patterns link-edges-patterns))]
        (vtree/add-new-version! db (qcont/get-qcontext-graph context))
        context)
      context)))

(defn set-processing [clause-data context db]
  (let [set-params (qextr/extract-set-params clause-data)
        [set-results context] (set-proc/set-processing set-params context)
        graph (jset/SET set-results (qcont/get-qcontext-graph context))
        context (qcont/set-qcontext-graph context graph)]
    (vtree/add-new-version! db graph)
    context))

(defn undo-processing [context db]
  (let [last-version-graph (vtree/undo! db)]
    (qcont/set-qcontext-graph context last-version-graph)))

(defn return-processing [clause-data context]
  (let [return-params (qextr/extract-return-params clause-data)
        all-variables (vutils/get-labels-properties-from-vars (qcont/get-qcontext-vars context))]
    (qcont/set-qcontext-return context (return-proc/return-processing return-params all-variables))))

(defn delete-processing [clause-data context db]
  (let [variables (qextr/extract-variables clause-data)
        new-context (vutils/delete-by-vars context variables)]
    (vtree/add-new-version! db (qcont/get-qcontext-graph new-context))
    new-context))

(defn create-processing [clause-data context db]
  (let [patterns (qextr/extract-patterns clause-data)
        [new-nodes-patterns new-edges-patterns] (patt-proc/patterns-processing patterns context)
        new-context (reduce (fn [context [new-nodes new-edges]]
                              (let [[new-nodes new-edges] (vutils/replace-nodes-edges-by-variables new-nodes new-edges
                                                                                                   (qcont/get-qcontext-vars context))
                                    context (->
                                              (vutils/add-variables-to-context context new-nodes qcont/add-qcontext-nodes-var)
                                              (vutils/add-variables-to-context new-edges qcont/add-qcontext-edges-var))

                                    updated-graph (->
                                                    (qcont/get-qcontext-graph context)
                                                    (jgraph/add-nodes (map vutils/get-var-value new-nodes))
                                                    (jgraph/add-edges (apply concat (map vutils/get-var-value new-edges))))
                                    new-context (qcont/set-qcontext-graph context updated-graph)]
                                new-context))
                            context
                            (map vector new-nodes-patterns new-edges-patterns))]
    (vtree/add-new-version! db (qcont/get-qcontext-graph new-context))
    new-context))

(defn saveviz-processing [clause-data context]
  (let [graph (qcont/get-qcontext-graph context)
        pathname (first clause-data)
        format-idx (last-index-of pathname ".")
        [pathname format] (if (nil? format-idx) [(str pathname ".png") "png"]
                                                [pathname (join (drop (+ format-idx 1) pathname))])
        ]
    (jgraphviz/save-graphviz graph pathname (keyword format))
    context))

(defn savejson-processing [clause-data context]
  (let [pathname (first clause-data)
        graph (qcont/get-qcontext-graph context)]
    (jgraph/save-graph graph pathname)
    context))

(defn loadjson-processing [clause-data context db]
  (let [pathname (first clause-data)
        new-graph (jgraph/load-graph pathname)
        context (qcont/set-qcontext-graph context new-graph)]
    (vtree/add-new-version! db new-graph)
    context))

(defmulti clause-processing (fn [clause context db] (qextr/extract-clause-name clause)))
(defmethod clause-processing :create [clause context db] (create-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :delete [clause context db] (delete-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :undo [clause context db] (undo-processing context db))
(defmethod clause-processing :match [clause context db] (match-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :set [clause context db] (set-processing (qextr/extract-clause-data clause) context db))
(defmethod clause-processing :return [clause context db] (return-processing (qextr/extract-clause-data clause) context))
(defmethod clause-processing :saveviz [clause context db] (saveviz-processing (qextr/extract-clause-data clause) context))
(defmethod clause-processing :savejson [clause context db] (savejson-processing (qextr/extract-clause-data clause) context))
(defmethod clause-processing :loadjson [clause context db] (loadjson-processing (qextr/extract-clause-data clause) context db))

(defn runner [db query params]
  (let [last-version-graph (vtree/get-last-version db)
        context-after-query (reduce (fn [context clause] (clause-processing clause context db))
                                    (qcont/get-qcontext last-version-graph params)
                                    (qextr/extract-clauses query))]
    (qcont/get-qcontext-return context-after-query)))

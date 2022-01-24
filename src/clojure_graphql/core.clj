(ns clojure-graphql.core
  (:require [clojure-graphql.impl.language-parser :as core-impl]

            [clojure-graphql.impl.versions-tree :as vtree]
            [clojure-graphql.impl.query-processing.clauses-processing :as impl]

            [jsongraph.api.graphviz :as jgraphviz]))


(defmacro defquery
  ([query_name query]
   `(let [parsed_query# ~(core-impl/create-rule query)]
      (def ~query_name (fn query_runner#
                         ([~'db] (query_runner# ~'db {}))
                         ([~'db ~'params] (impl/runner ~'db parsed_query# ~'params)))))))

(defn init-db []
  (vtree/init-db))

(defn show-last-version [db]
  (-> (vtree/get-last-version db)
      jgraphviz/show-graphviz))

(defn print-last-version [db]
  (clojure.pprint/pprint (vtree/get-last-version db)))

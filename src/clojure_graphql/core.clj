(ns clojure-graphql.core
  (:require [clojure-graphql.impl.language_parser :as core-impl])
  (:require [clojure-graphql.impl.versions-tree :as vtree]
            [clojure-graphql.impl.clauses_processing :as impl]))


(defmacro defquery
  ([query_name query]
   `(let [parsed_query# ~(core-impl/create-rule query)]
      (def ~query_name (fn query_runner#
                         ([~'db] (query_runner# ~'db {}))
                         ([~'db ~'params] (impl/runner ~'db parsed_query# ~'params)))))))

(defn init-db []
  (vtree/init-db))

(defn print-last-version [db]
  (clojure.pprint/pprint (vtree/get-last-version db)))

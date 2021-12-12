(ns clojure-graphql.core
  (:require [clojure-graphql.impl.core-impl :as core-impl]))


(defmacro defquery
  ([query_name query]
   `(let [parsed_query# ~(core-impl/create-rule query)]
      (def ~query_name (fn query_runner#
                         ([~'session] (query_runner# ~'session {}))
                         ([~'session ~'params] (~'session parsed_query# ~'params)))))))

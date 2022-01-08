(ns clojure-graphql.impl.variables-utils
  (:require [clojure.string :refer [blank?]])
  (:require [clojure-graphql.impl.query-context :refer [get-qcontext-var]]))

(defn create-variable [var-name var-value]
  {:var-name var-name :var-value var-value})

(defn get-var-value [var]
  (:var-value var))

(defn get-var-name [var]
  (:var-name var))

(defn add-variables-to-context [context variables adder]
  (reduce (fn [context var]
            (let [var-name (get-var-name var)
                  var-val (get-var-value var)]
              (if (not (blank? var-name))
                (let [var-by-name (get-qcontext-var context var-name)]
                  (if (nil? var-by-name)
                    (adder context var-name [var-val])
                    context))
                context)))
          context
          variables))z

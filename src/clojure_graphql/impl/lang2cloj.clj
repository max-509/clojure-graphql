(ns clojure-graphql.impl.lang2cloj
  (:require [clojure-graphql.impl.query_extracter :as qextr]))

(def command-map
  {:lt-command          :lt
   :le-command          :le
   :gt-command          :gt
   :ge-command          :ge
   :eq-command          :eq
   :ne-command          :ne
   :is-null-command     :is-null
   :is-not-null-command :is-not-null
   :starts-with-command :starts-with
   :ends-with-command   :ends-with
   :contains-command    :contains
   :like-regex-command  :like-regex
   :in-command          :in})

(defn convert-command [command]
  (get command-map command))

(def operators-map
  {:and-command :and
   :or-command :or
   :negation-command :neg})

(defn convert-operator [op]
  (get operators-map op))

(defmulti convert-prop-value (fn [val] (qextr/extract-property-val-type val)))
(defmethod convert-prop-value :integer [val] (Integer/parseInt (qextr/extract-property-val-data val)))
(defmethod convert-prop-value :float [val] (Double/parseDouble (qextr/extract-property-val-data val)))
(defmethod convert-prop-value :boolean [val] (Boolean/parseBoolean (qextr/extract-property-val-data val)))
(defmethod convert-prop-value :string [val] (qextr/extract-property-val-data val))
(defmethod convert-prop-value :list [val]
  (let [list-values (qextr/extract-property-val-data val)]
    (if (empty? list-values)
      []
      (mapv #(convert-prop-value %) list-values))))

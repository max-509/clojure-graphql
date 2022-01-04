(ns jsongraph.impl.query.where
  (:require [jsongraph.impl.utils :refer :all])
  (:require [clojure.string :refer :all]))

(defmacro xor
  "Evaluates exprs one at a time, from left to right.  If only one form returns
  a logical true value (neither nil nor false), returns true.  If more than one
  value returns logical true or no value returns logical true, retuns a logical
  false value.  As soon as two logically true forms are encountered, no
  remaining expression is evaluated.  (xor) returns nil."
  ([] nil)
  ([f & r]
   `(loop [t# false f# '[~f ~@r]]
      (if-not (seq f#) t#
                       (let [fv# (eval (first f#))]
                         (cond
                           (and t# fv#) false
                           (and (not t#) fv#) (recur true (rest f#))
                           :else (recur t# (rest f#))))))))

(defn get-node-type [node]
  (first node))

(defn get-node-value [node]
  (second node))

(defn node-bin-op? [node-type]
  (= :binary-op node-type))

(defn node-un-op? [node-type]
  (= :unary-op node-type))

(defn node-pred? [node-type]
  (= :pred node-type))

(defn like-regex? [s re]
  (not= nil (re-find (re-pattern re) s)))

(def comparing-operations-map
  {:eq =  :ne not=
   :gt >  :lt <
   :ge >= :le <=
   :starts-with starts-with?
   :ends-with ends-with?
   :contains includes?
   :like-regex like-regex?
   :contains contains?})

(defn compare [property-val cmp-val cmp-op]
  (let [cmp-func (get comparing-operations-map cmp-op)]
    (if (= nil cmp-func)
      (throw (RuntimeException. (str "Error: Not supported comparing operation: " cmp-op)))
      (cmp-func property-val cmp-val))))

(defn field-check-processing [pattern check-value]
  (let [var-name (get (nth check-value 0) :name)
        prop-name (get (nth check-value 0) :property)
        cmp-op (nth check-value 1)
        cmp-val (nth check-value 2)
        var-by-name (filter (fn [var]
                              (= var-name (get var :var-name)))
                            pattern)]
    (if (empty? var-by-name)
      (throw (RuntimeException. (str "Error: Variable with name '" var-name "' wasn't in MATCH-clause")))
      (let [var-by-name (first var-by-name)
            property-val (get (get-field var-by-name :properties) prop-name)]
        (compare property-val cmp-val cmp-op)))))

(defn predicate-processing [pattern pred]
  (let [check-type (get pred :type)
        check-value (get pred :val)]
    (cond
      (= :field-check check-type) (field-check-processing pattern check-value)
      :default (throw (RuntimeException. (str "Error: Not supported predicate type: " check-type))))))

(defn expr-tree-walk [pattern expr-tree]
  (let [node (first expr-tree)
        node-type (get-node-type node)
        node-value (get-node-value node)]
    (cond
      (node-bin-op? node-type) (let [op node-value
                                l-node (nth expr-tree 1)
                                r-node (nth expr-tree 2)]
                            (cond
                              (= :and op) ((and (expr-tree-walk pattern l-node) (expr-tree-walk pattern r-node)))
                              (= :or op) ((or (expr-tree-walk pattern l-node) (expr-tree-walk pattern r-node)))
                              (= :xor op) ((xor (expr-tree-walk pattern l-node) (expr-tree-walk pattern r-node)))
                              :default (throw (RuntimeException. (str "Error: Not supported binary operation: " op)))))
      (node-un-op? node-type) (let [op node-value
                               next-node (nth expr-tree 1)]
                           (cond
                             (= :not op) (not (expr-tree-walk pattern next-node))
                             :default (throw (RuntimeException(. str "Error: Not supported unary operation: " op)))))
      (node-pred? node-type) (predicate-processing pattern node-value)
      :default (throw (RuntimeException. (str "Error: Not supported type of node expression: " node-type))))))


(defn filtered-pattern? [pattern expr-tree]
  (expr-tree-walk pattern expr-tree))

(defn where-filter [founded-patterns expr-tree]
  (filter (fn [pattern] (filtered-pattern? pattern expr-tree))
          founded-patterns))

(ns jsongraph.impl.query.where
  (:require [clojure.string :refer [starts-with? ends-with? includes?]]

            [jsongraph.api.graph-api :refer [gen-edge-data gen-node]]
            [jsongraph.impl.graph :refer [get-edge-source get-edge-target get-edge-data]]))

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

(defn- get-node-type [node]
  (first node))

(defn- get-node-value [node]
  (second node))

(defn- node-bin-op? [node-type]
  (= :binary-op node-type))

(defn- node-un-op? [node-type]
  (= :unary-op node-type))

(defn- node-pred? [node-type]
  (= :pred node-type))

(defn- like-regex? [s re]
  (not= nil (re-find (re-pattern re) s)))

(def comparing-operations-map
  {:eq          = :ne not=
   :gt          > :lt <
   :ge          >= :le <=
   :starts-with starts-with?
   :ends-with   ends-with?
   :contains    includes?
   :like-regex  like-regex?
   :in          contains?})

(def bin-ops-map
  {:and (fn [x y] (and x y))
   :or  (fn [x y] (or x y))
   :xor (fn [x y] (xor x y))})

(def un-ops-map
  {:neg (fn [x] (not x))})

(defn- compare-prop-val [property-val cmp-val cmp-op]
  (let [cmp-func (get comparing-operations-map cmp-op)]
    (if (some? cmp-func)
      (cmp-func property-val cmp-val)
      (throw (RuntimeException. (str "Error: Not supported comparing operation: " cmp-op))))))

(defn- field-check-processing [pattern check-value]
  (let [var-name (get (nth check-value 0) :name)
        prop-name (keyword (get (nth check-value 0) :property))
        cmp-op (nth check-value 1)
        cmp-val (nth check-value 2)
        var-by-name (get pattern var-name)]
    (if (some? var-by-name)
      (let [property-val (get (:properties var-by-name) prop-name)]
        (if (some? property-val)
          (compare-prop-val property-val cmp-val cmp-op)
          false))
      (throw (RuntimeException. (str "Error: Variable with name '" var-name "' wasn't in MATCH-clause"))))))

(defn- predicate-processing [pattern pred]
  (let [check-type (get pred :type)
        check-value (get pred :val)]
    (cond
      (= :field-check check-type) (field-check-processing pattern check-value)
      :default (throw (RuntimeException. (str "Error: Not supported predicate type: " check-type))))))

(defn- expr-tree-walk [pattern expr-tree]
  (let [node (first expr-tree)
        node-type (get-node-type node)
        node-value (get-node-value node)]
    (cond
      (node-bin-op? node-type) (let [op node-value
                                     l-node (nth expr-tree 1)
                                     r-node (nth expr-tree 2)
                                     bin-op (get bin-ops-map op)]
                                 (if (some? bin-op)
                                   (bin-op (expr-tree-walk pattern l-node) (expr-tree-walk pattern r-node))
                                   (throw (RuntimeException. (str "Error: Not supported binary operation: " op)))))
      (node-un-op? node-type) (let [op node-value
                                    next-node (nth expr-tree 1)
                                    un-op (get un-ops-map op)]
                                (if (some? un-op)
                                  (un-op (expr-tree-walk pattern next-node))
                                  (throw (RuntimeException. (str "Error: Not supported unary operation: " op)))))
      (node-pred? node-type) (predicate-processing pattern node-value)
      :default (throw (RuntimeException. (str "Error: Not supported type of node expression: " node-type))))))


(defn- filtered-pattern? [pattern expr-tree]
  (if (nil? expr-tree)
    true
    (expr-tree-walk pattern expr-tree)))

(defn where-filter [ways founded-patterns nodes edges expr-tree]
  (filter
    (fn [[vars-nodes vars-edges]]
      (let [labels-props-nodes (into {} (map (fn [[node-name node]]
                                               (let [node-val (second (first (seq node)))
                                                     labels (:labels node-val)
                                                     properties (:properties node-val)]
                                                 [node-name {:labels labels :properties properties}]))
                                             (seq vars-nodes)))
            labels-props-edges (into {} (map (fn [[edge-name [edge]]]
                                               (let [labels-properties (get-edge-data edge)]
                                                 [edge-name labels-properties]))
                                             (seq vars-edges)))]
        (filtered-pattern? (merge labels-props-nodes labels-props-edges) expr-tree)))
    (map (fn [[way pattern]]
           (let [vars-uuids (mapv vector nodes way)
                 varnames-to-uuids (into {} (map (fn [[var uuid]] [(get var :var-name) uuid]) vars-uuids))
                 vars-nodes (into {} (map (fn [[var uuid]]
                                            (let [var-name (:var-name var)
                                                  node (get pattern uuid)]
                                              [var-name {uuid node}]))
                                          vars-uuids))
                 vars-edges (into {} (map (fn [var]
                                            (let [var-name (:var-name var)
                                                  edge (:var-value var)
                                                  source-uuid (get varnames-to-uuids (get-edge-source edge))
                                                  target-uuid (get varnames-to-uuids (get-edge-target edge))
                                                  source {source-uuid (get pattern source-uuid)}
                                                  target {target-uuid (get pattern target-uuid)}
                                                  labels-properties (get (:out-edges (get source source-uuid)) target-uuid)
                                                  labels (:labels labels-properties)
                                                  properties (:properties labels-properties)]
                                              [var-name [(gen-edge-data source target labels properties)]]))
                                          edges))]
             [vars-nodes vars-edges]))
         (mapv vector ways founded-patterns))))

(ns clojure-graphql.impl.query_processing.where-processing
  (:require [clojure-graphql.impl.predicates-extracter :as pextr]))

(use '[clojure.pprint :only (pprint)])

(defn above-priority? [op1 op2]
  (if (= op2 :left-bracket)
    false
    (if (= op2 :negation-command)
      true
      (if (and (= op2 :and-command) (not= op1 :negation-command))
        true
        (not (and (= op2 :or-command) (= op1 :and-command)))))))

(defn processing-token-binary-operator [op-stack rpn bin-op]
  (loop [op-stack op-stack
         rpn rpn
         bin-op bin-op]
    (if (empty? op-stack)
      [op-stack (conj rpn bin-op)]
      (let [last-op (first op-stack)]
        (if (above-priority? bin-op last-op)
          (recur (rest op-stack) (conj rpn last-op) bin-op)
          [op-stack (conj rpn bin-op)])))))

(defn inf-predicates-to-rpn
  ([predicates] (inf-predicates-to-rpn predicates (list []) []))
  ([predicates op-stack rpn]
   (pprint "inf-predicates-to-rpn")
   (pprint predicates)
   (pprint op-stack)
   (pprint rpn)
   (reduce
     (fn [[op-stack rpn] token]
       (if (pextr/predicate? token)
         (let [predicate (pextr/extract-token-value token)
               predicate-value (pextr/extract-predicate-value predicate)
               predicate-expression (pextr/extract-predicate-expression predicate-value)
               [op-stack rpn] (if (pextr/negation-predicate-value? predicate-value)
                                [(cons :negation-command op-stack) rpn]
                                [op-stack rpn])]
           (if (pextr/brackets-predicates? predicate)
             (let [[op-stack rpn] [(cons :left-bracket op-stack) rpn]]
               (inf-predicates-to-rpn predicate-expression op-stack rpn))
             [op-stack (conj rpn predicate-expression)]))
         (let [bin-op (pextr/extract-token-value token)]
           (processing-token-binary-operator op-stack rpn bin-op))))
     [op-stack rpn]
     (pextr/extract-tokens predicates))))

(defn where-processing [predicates context]
  (pprint "where")
  (pprint predicates)
  (if (empty? predicates)
    nil
    nil))
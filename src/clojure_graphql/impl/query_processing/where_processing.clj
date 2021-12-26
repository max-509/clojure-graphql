(ns clojure-graphql.impl.query_processing.where-processing
  (:require [clojure-graphql.impl.predicates-extracter :as pextr]))

(use '[clojure.pprint :only (pprint)])

(defn above-priority? [op1 op2]
  (if (= :left-bracket op2)
    false
    (if (= :negation-command op2)
      true
      (if (and (= :and-command op2) (not= :negation-command op1))
        true
        (not (and (= :or-command op2) (= :and-command op1)))))))

(defn processing-token-binary-operator [op-stack rpn bin-op]
  (loop [op-stack op-stack
         rpn rpn
         bin-op bin-op]
    (if (empty? op-stack)
      [op-stack (conj rpn bin-op)]
      (let [last-op (first op-stack)]
        (if (above-priority? bin-op last-op)
          (recur (rest op-stack) (conj rpn last-op) bin-op)
          [(cons bin-op op-stack) rpn])))))

(defn pop-ops-while-no-left-bracket [op-stack rpn]
  (loop [op-stack op-stack
         rpn rpn]
    (if (empty? op-stack)
      (throw (RuntimeException. "Error: cannot find left bracket"))
      (let [op (first op-stack)]
        (if (= :left-bracket op)
          [(rest op-stack) rpn]
          (recur (rest op-stack) (conj rpn op)))))))

(defn inf-predicates-to-rpn
  ([predicates] (inf-predicates-to-rpn predicates (list :left-bracket) []))
  ([predicates op-stack rpn]
   (pprint "inf-predicates-to-rpn")
   (pprint predicates)
   (pprint op-stack)
   (pprint rpn)
   (let [[new-op-stack new-rpn] (reduce
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
                                  (pextr/extract-tokens predicates))]
     (pop-ops-while-no-left-bracket new-op-stack new-rpn))))

(defn rpn-predicates-to-bin-expr-tree [rpn context]
  (pprint "rpn")
  (pprint rpn))

(defn where-processing [predicates context]
  (pprint "where")
  (pprint predicates)
  (if (empty? predicates)
    nil
    (let [[op-stack rpn] (inf-predicates-to-rpn predicates)]
      (if (seq op-stack)
        (throw (RuntimeException. "Error: Stack operations not empty - infix expression is bad"))
        (rpn-predicates-to-bin-expr-tree rpn context)))))
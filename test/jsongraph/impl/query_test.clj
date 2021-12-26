(ns jsongraph.impl.query-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query :refer :all]
            [jsongraph.api.graph-api :refer [gen-node gen-edge create-graph]]
            [jsongraph.api.match-api :refer :all]))


(def prop-A {:money 100 :age 21 :weight 50})
(def nA  (gen-node [:lab-A] prop-A :A))

(def query-where-matched {:money [:gt 70] :age [:lt 30] :weight [:eq 50]})
(def query-where-no-matched {:money [:lt 70] :age [:gt 30] :weight [:eq 50]})
(def query-where-on-matched-unknown {:age [:gt 18] :cost [:eq 45]})

(def query-node-matched   (gen-query-node [:lab-A] {} query-where-matched))
(def query-node-matched-2 (gen-query-node [:lab-A] {} {:age [:gt 18] :weight [:ne 100]}))

(def query-node-no-matched         (gen-query-node [:lab-A] {} query-where-no-matched))
(def query-node-no-matched-unknown (gen-query-node [:lab-A] {} query-where-on-matched-unknown))
(def query-node-no-matched-lab     (gen-query-node [:lab-B] {} query-where-matched))

(def query-node-with-any-where (gen-query-node [:lab-A] {} nil))

(deftest match-node-where-properties-test
  (testing "match")
  (is (match-where-properties nA query-node-matched))
  (is (match-where-properties nA query-node-matched-2))
  (is (match-where-properties nA query-node-with-any-where))
  (testing "no match")
  (is (not (match-where-properties nA query-node-no-matched)))
  (testing "unknown keys")
  (is (not (match-where-properties nA query-node-no-matched-unknown)))
  )

(deftest match-node-test
  (testing "match")
  (is (match-node nA query-node-matched))
  (is (match-node nA query-node-with-any-where))
  (testing "no match")
  (is (not (match-node nA query-node-no-matched-lab)))
  (is (not (match-node nA query-node-no-matched)))
  )

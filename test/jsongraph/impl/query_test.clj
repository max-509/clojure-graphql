(ns jsongraph.impl.query-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query :refer :all]
            [jsongraph.api :refer [gen-node gen-edge create-graph]]))


(def prop-A {:money 100 :age 21 :weight 50})
(def prop-B {:money 380 :age 30 :weight 64})
(def prop-edge {:cost 68 :danger 10})


(def nA  (gen-node [:lab-A] prop-A :A))
(def nB  (gen-node [:lab-B] prop-B :B))
(def edge-AB (gen-edge nA nB nil prop-edge))




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



(def query-node-matched-A    (gen-query-node [:lab-A] {} query-where-matched   :A))
(def query-node-no-matched-A (gen-query-node [:lab-A] {} query-where-no-matched :A))

(def query-edge (gen-query-edge :B nil nil {:cost [:lt 75] :danger [:gt 5]}))

(def query-node-matched-B (gen-query-node [:lab-B] {}
                                          {:money [:gt 200] :age [:gt 25] :weight [:ne 60]} :B))
(def query-node-no-matched-B (gen-query-node [:lab-B] {}  query-where-on-matched-unknown :B))

(def query-node-with-edge-matched
  (add-edge-into-query-node query-node-matched-A query-edge query-node-matched-B))

(def query-node-with-edge-no-matched-A
  (add-edge-into-query-node query-node-no-matched-A query-edge query-node-matched-B))
(def query-node-with-edge-no-matched-B
  (add-edge-into-query-node query-node-matched-A query-edge query-node-no-matched-B))


(def graph (create-graph [nA nB] [edge-AB]))

(deftest match-query-test
  (testing "match")
  (println (match-query graph query-node-with-edge-matched))
  (testing "no match")
  (println (match-query graph query-node-with-edge-no-matched-A))
  (println (match-query graph query-node-with-edge-no-matched-B))
  )
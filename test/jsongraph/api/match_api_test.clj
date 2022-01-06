(ns jsongraph.api.match-api-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.match-test :refer :all]
            [jsongraph.api.match-api :refer :all]))
(use '[clojure.pprint :only (pprint)])

(deftest match-node-test
  (testing "match") (println "match")
  (println (match-nodes nA query-node-matched-A))
  (println (match-nodes nA query-node-matched-any))
  (testing "no match") (println "no match")
  (println (match-nodes nA query-node-no-matched-lab))
  (println (match-nodes nA query-node-no-matched)))

(deftest match-query-graph-wo-edges-test
  (println "Format: full data match answer")
  (testing "match") (println "match")
  (println (match-query graph-nA query-node-matched-A))
  (println (match-query graph-nA-nB query-node-matched-any))
  (testing "no match") (println "no match")
  (println (match-query graph-empty query-node-matched-A))
  (println (match-query graph-empty query-node-matched-any)))


(deftest match-query-graph-with-edges-test
  (println "Format: match-answer")
  (testing "match")(println "Match")
  (println " Any nodes\t\t\t\t   ") (println (match-query graph-with-edge query-node-matched-any))
  (println " Source in graph with edge ") (println (match-query graph-with-edge query-node-matched-A))
  (println " Full edge\t\t\t\t   ") (pprint (match-query graph-with-edge query-node-with-edge-matched-AB))

  (println " Any nodes in big graph\t   ") (println (match-query graph-with-edges query-node-matched-any))
  (println " Source to any\t\t\t   ") (pprint (match-query graph-with-edges query-node-with-edge-matched-A-any))
  (println " Any to any\t\t\t\t   ") (pprint (match-query graph-with-edges query-node-with-edge-matched-any-any))
  (testing "no match") (println "NO Match")
  (print " Source\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-A))
  (print " Target\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-B))
  (print " Edge\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-edge-AB)))
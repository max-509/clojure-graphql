(ns jsongraph.api.match-api-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.match-test :refer :all]
            [jsongraph.api.match-api :refer :all]))
(use '[clojure.pprint :only (pprint)])

(deftest match-query-graph-wo-edges-test
  (testing "match") (println "match")
  (println (match-query graph-nA query-node-matched-A))
  (println (match-query graph-nA-nB query-node-matched-any))
  (testing "no match") (println "no match")
  (println (match-query graph-empty query-node-matched-A))
  (println (match-query graph-empty query-node-matched-any)))


(deftest match-query-graph-with-edges-test
  (println "Match")
  (print " Any nodes\t\t\t\t   ") (println (match-query graph-with-edge query-node-matched-any))
  (print " Source in graph with edge ") (println (match-query graph-with-edge query-node-matched-A))
  (print " Full edge\t\t\t\t   ") (println (match-query graph-with-edge query-node-with-edge-matched-A-B))
  (print " Any nodes in big graph\t   ") (println (match-query graph-with-edges query-node-matched-any))
  (print " Source to any\t\t\t   ") (println (match-query graph-with-edges query-node-with-edge-matched-A-any))
  (print " Any to any\t\t\t\t   ") (println (match-query graph-with-edges query-node-with-edge-matched-any-any))
  (println "NO Match")
  (print " Source\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-A))
  (print " Target\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-B))
  (print " Edge\t ") (println (match-query graph-with-edge query-node-with-edge-matched-edge-A-B)))


(deftest node-to-query-node-test
  (pprint nA)
  (pprint (node-to-query-node nA query-where-matched)))

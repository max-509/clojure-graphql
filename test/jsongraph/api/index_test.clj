(ns jsongraph.api.index-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.match-test :refer [prop-A prop-B prop-C query-node-matched-B
                                                     big-graph query-loop-AA  query-node-matched-A
                                                     query-node-matched-any query-node-with-edge-no-matched-AB
                                                     query-node-with-edge-matched-A-any query-node-with-AB-AC-edges
                                                     query-node-with-edge-matched-any-any ]]
            [jsongraph.api.match-api :refer [match-query]]
            [jsongraph.impl.graph-test :refer [full-graph]]
            [jsongraph.api.graph-api :refer [gen-node create-graph
                                             add-labels-index delete-labels-index]]))

(use '[clojure.pprint :only (pprint)])


(def nA  (gen-node [:lab-A] prop-A :A))
(def nB  (gen-node [:lab-B] prop-B :B))
(def nC  (gen-node [:lab-C] prop-C :C))
(def nD  (gen-node [:lab-C] prop-C :D))
(def nE  (gen-node [:lab-B] prop-B :E))
(def nF  (gen-node [:lab-B] prop-B :F))
(def nG  (gen-node [:lab-C] prop-C :G))
(def nH  (gen-node [:lab-C] prop-C :H))
(def nI  (gen-node [] {} :I))
(def nJ  (gen-node [] {} :J))


(def nodes-graph (create-graph [nA nB nC nD nE nF nG nH nI nJ]))
(def indexed-graph (add-labels-index nodes-graph [:lab-A :lab-B :lab-C]))
(def indexed-big-graph (add-labels-index big-graph [:lab-A :lab-B :lab-C :lab-D]))

(deftest index-test
  (pprint (nodes-graph  :metadata))
  (pprint (indexed-graph  :metadata))
  (pprint ((delete-labels-index indexed-graph [:lab-A :lab-B :lab]) :metadata))
  (pprint ((delete-labels-index indexed-graph [:lab-A :lab-B :lab-C ])  :metadata))
  (pprint ((delete-labels-index indexed-graph [:lab-A :lab-B :lab-4 :lab-t]) :metadata)))



(deftest match-query-nodes-graph-demo
  (println "nodes-graph") (pprint nodes-graph)
  (println (match-query nodes-graph query-node-matched-A true))
  (println (match-query nodes-graph query-node-matched-B true)))

(deftest match-query-indexed-graph-demo
  (println "indexed-graph") (pprint indexed-graph)
  (println (match-query indexed-graph query-node-matched-A true))
  (println (match-query indexed-graph query-node-matched-B true)))

;=================================================================================

(deftest match-query-big-graph-demo
  (println "big-graph") (pprint big-graph)
  (println (match-query big-graph query-node-matched-any true))
  (println (match-query big-graph query-loop-AA true))
  (println (match-query big-graph query-node-with-edge-no-matched-AB true))
  (println (match-query big-graph query-node-with-AB-AC-edges true))
  (println (match-query big-graph query-node-with-edge-matched-A-any true))
  (println (match-query big-graph query-node-with-edge-matched-any-any true)))

(deftest match-query-indexed-big-graph-demo
  (println "indexed-big-graph") (pprint indexed-big-graph)
  (println (match-query indexed-big-graph query-node-matched-any true))
  (println (match-query indexed-big-graph query-loop-AA true))
  (println (match-query indexed-big-graph query-node-with-edge-no-matched-AB true))
  (println (match-query indexed-big-graph query-node-with-AB-AC-edges true))
  (println (match-query indexed-big-graph query-node-with-edge-matched-A-any true))
  (println (match-query indexed-big-graph query-node-with-edge-matched-any-any true)))
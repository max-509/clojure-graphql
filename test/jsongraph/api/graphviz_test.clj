(ns jsongraph.api.graphviz-test
  (:require [clojure.test :refer :all]
            [jsongraph.api.graphviz :refer :all]

            [jsongraph.impl.utils :refer [get-key get-field split-json concat!]]
            [dorothy.jvm :refer (render save! show!)]
            [jsongraph.impl.graph-test :refer [full-graph]]
            [jsongraph.impl.query.match-test :refer [graph-with-edges big-graph]]))

(use '[clojure.pprint :only (pprint)])

 (def simple
   "digraph {
       A [label = nil color=black]
       A -> B [label = \"Edge A to B\" color = blue]
       A -> C [label = \"Edge A to C\" ]
       B -> C [label = \"Edge B to C\" ]
   }" )


(deftest graph-to-graphviz-save-demo
  (save-graphviz big-graph "big-graph.svg" :svg))

(defn wait [sec]
  (doseq [x (range sec)]
    (Thread/sleep 1000)
    (print x)))


; Run This For Get visualization
(def wait-seconds (* 5 60))
; This test will be active `wait-seconds` seconds
; If you want stop test you need just stop runtime
(deftest full-visualization-demo
  (testing "text format")
  (pprint graph-with-edges)
  (println simple)
  (println (graph2graphviz graph-with-edges))

  (testing "show and save")
  (show! simple)

  (show-graphviz full-graph)
  (save-graphviz full-graph "full-graph.svg" :svg)

  (show-graphviz graph-with-edges)
  (save-graphviz graph-with-edges "graph-with-edges.svg" :svg)

  (wait wait-seconds))

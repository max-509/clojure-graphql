(ns jsongraph.api.graphviz-test
  (:require [clojure.test :refer :all]
            [jsongraph.api.graphviz :refer :all]

            [jsongraph.impl.utils :refer [get-key get-field split-json concat!]]
            [dorothy.jvm :refer (render save! show!)]
            [jsongraph.impl.graph-test :refer [full-graph]]
            [jsongraph.impl.query.match-test :refer [graph-with-edges big-graph]]))

(use '[clojure.pprint :only (pprint)])

(def path-to-images "./resources/images/")

 (def simple
   "digraph {
       A [label = nil color=black]
       A -> B [label = \"Edge A to B\" color = blue]
       A -> C [label = \"Edge A to C\" ]
       B -> C [label = \"Edge B to C\" ]
   }" )


(deftest graph-to-graphviz-save-demo
  (save! (graph2graphviz big-graph) (str path-to-images "big-graph.svg") {:format :svg}))

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
  (save! simple (str path-to-images "simple.svg") {:format :svg})

  (show! (graph2graphviz full-graph))
  (save! (graph2graphviz full-graph) (str path-to-images "full-graph.svg") {:format :svg})

  (show! (graph2graphviz graph-with-edges))
  (save! (graph2graphviz graph-with-edges) (str path-to-images "graph-with-edges.svg") {:format :svg})

  (wait wait-seconds))

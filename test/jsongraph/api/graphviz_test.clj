(ns jsongraph.api.graphviz-test
  (:require [clojure.test :refer :all]
            [jsongraph.api.graphviz :refer :all]

            [jsongraph.impl.utils :refer [get-key get-field split-json concat!]]
            [dorothy.jvm :refer (render save! show!)]
            [jsongraph.impl.graph-test :refer [full-graph]]
            [jsongraph.impl.query.match-test :refer [graph-with-edges]]))

(use '[clojure.pprint :only (pprint)])

(def path-to-images "./resources/images/")

 (def simple
   "digraph {
       A [label = nil color=black]
       A -> B [label = \"Edge A to B\" color = blue]
       A -> C [label = \"Edge A to C\" ]
       B -> C [label = \"Edge B to C\" ]
   }" )



(deftest get-graphviz-edges-test
  (pprint (get-graphviz-edges
    (first (split-json (full-graph :adjacency))))))

(deftest graph-to-graphviz-test
  (save! (graph-to-graphviz full-graph) (str path-to-images "full-graph.svg") {:format :svg}))

await-for
(defn wait
  [sec]
  (doseq [x (range sec)]
    (Thread/sleep 1000)
    (print x)))

(deftest vis-test
  (pprint graph-with-edges)
  (println simple)
  (println (graph-to-graphviz graph-with-edges))
  (show! simple)
  (save! simple (str path-to-images "simple.svg") {:format :svg})
  (show!  (graph-to-graphviz full-graph))
  (save! (graph-to-graphviz full-graph) (str path-to-images "full-graph.svg") {:format :svg})
  (show!  (graph-to-graphviz graph-with-edges))
  (save! (graph-to-graphviz graph-with-edges) (str path-to-images "graph-with-edges.svg") {:format :svg})
  (wait 1000))

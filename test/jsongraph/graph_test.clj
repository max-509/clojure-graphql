(ns jsongraph.graph-test
  (:require [clojure.test :refer :all]
            [jsongraph.graph :refer :all]
            [jsongraph.utils :refer :all]
    ;[jsonista.core :as j] maybe use in future
            [clojure.data.json :as json]
            )
  (:import (java.io File))
)

(use '[clojure.pprint :only (pprint)])


(def file    (File. "./resources/graph.json"))
(def file_e  (File. "./resources/graph_empty.json"))
(def file_b  (File. "./resources/graph_byte.json"))

(def nA (gen-node :A {}))
(def nB (gen-node :B {}))
(def nC (gen-node :C {}))
(def nD (gen-node :D {}))

(def g-add-nodes (add-node (add-node (gen-empty-graph) [nA nB nC]) nD))

(def edgeAB  '([:A :B] {:cost 1}))   ;edge format '([source target] data)
(def edgeAC  '([:A :C] {:cost 4}))

(def edgeBA  '([:B :A] {:cost 2}))
(def edgeBA- '([:B :A] {:cost 3}))
(def edgeDA  '([:D :A] {:cost 8}))


(deftest delete-node-test
  (println)
  (println "delete-node-test")
  (json/pprint g-add-nodes)
  (json/pprint (delete-node g-add-nodes [:A :B]))
  )


(deftest adjacency-from-edges-test
  (println)
  (println "adjacency-from-edges-test")
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA])   ;edgeBA- rewrite data after edgeBA
  (json/pprint (adjacency-from-edges [edgeAB edgeAC edgeBA edgeBA- edgeDA]))
  )

(deftest add-out-edges-test
  (println)
  (println "add-out-edges-test")
  (json/pprint (g-add-nodes :adjacency))
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA])
  (println "result")
  (json/pprint (add-out-edges (g-add-nodes :adjacency) [edgeAB edgeAC edgeBA edgeBA- edgeDA]))
  )

(deftest add-edge-test
  (println)
  (println "add-edge-test")
  (json/pprint g-add-nodes)
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA])
  (println "result")
  (json/pprint (add-edge g-add-nodes [edgeAB edgeAC edgeBA edgeBA- edgeDA]))
  )

(comment
(def full-graph (add-edge g-add-nodes [edgeAB edgeAC edgeBA edgeBA- edgeDA]))

(deftest delete-edge-test
  (println)
  (println "delete-edge-test")
  (json/pprint (full-graph :adjacency))
  (println :A [:B])
  (json/pprint (delete-edge (full-graph :adjacency) :A [:B]))
  )
)
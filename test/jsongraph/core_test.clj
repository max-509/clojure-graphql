(ns jsongraph.core-test
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

(def eAB (gen-edge :AB [:A :B] {:t "d"}))
(def eBA (gen-edge :BA [:B :A] {:t "d"}))
(def eBA (gen-edge :BA [:B :A] {:t "d"}))


(deftest a-test
  (println (coll? 'a))
  (json/pprint g-add-nodes)
  (json/pprint (delete-node g-add-nodes [:A :B]))
  )

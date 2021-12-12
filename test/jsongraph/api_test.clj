(ns jsongraph.api-test
  (:require [clojure.test :refer :all]
            [jsongraph.api :refer :all]
            [clojure.data.json :as json]))


(def nA (gen-node [:label-A] {}))
(def nB (gen-node [:label-B] {}))
(def nC (gen-node [:label-C] {}))


(def edgeAB  (gen-edge nA nB [] {:cost 1}))
(def edgeAC  (gen-edge nA nC [] {:cost 4}))
(def edgeBA  (gen-edge nB nA [] {:cost 2}))



(deftest create-graph-test
  (json/pprint (create-graph))
  (json/pprint (create-graph [nA nB nC]))
  (json/pprint (create-graph [nA nB nC] [edgeAB edgeAC edgeBA]))
  )
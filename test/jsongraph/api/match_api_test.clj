(ns jsongraph.api.match-api-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.match-test :refer :all]
            [jsongraph.api.match-api :refer :all]))
(use '[clojure.pprint :only (pprint)])


(deftest node-to-query-node-test
  (pprint nA)
  (pprint (node-to-query-data nA)))

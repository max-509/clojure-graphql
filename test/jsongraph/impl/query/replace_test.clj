(ns jsongraph.impl.query.replace-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.replace :refer :all]
            [jsongraph.api.graph-api :refer :all]
            [jsongraph.impl.graph-test :refer [full-graph]]
            ))

(use '[clojure.pprint :only (pprint)])




(deftest construct-template-graph-test
  (pprint (construct-template-graph full-graph)))

(deftest construct-template-graph-some-nodes-test
  (pprint (construct-template-graph full-graph [:A :D])))
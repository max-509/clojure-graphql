(ns clojure-graphql.core-test
  (:require [clojure.test :refer :all]
            [clojure-graphql.core :refer :all]))

(println (create-rule "create (:Person :Manager {name: \"Emil\" from: \"Sweden\" klout: 99})"))


;(defquery test-query "create (:Person :Manager {:name \"Emil\", :from \"Sweden\", :klout 99}) - [:FRIEND {:duration \"Forever\"}] -> (:Person :Director {:name \"Frank\", :from \"USA\"})")

;(test-query 1)

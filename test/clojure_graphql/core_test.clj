(ns clojure-graphql.core-test
  (:require [clojure.test :refer :all]
            [clojure-graphql.core :refer :all]
            [clojure-graphql.impl.language_parser :refer :all]))

(use '[clojure.pprint :only (pprint)])

;(println (rest [1]))

(defquery get-persons "create (a:Person:Manager {name: 'Emil' from: 'Sweden' klout: 99 list: [99 0 1] listt: []})
                      match (a:Person) where a.name = 'Emil' AND a.klout < 100 AND NOT (a.klout > 50 OR a.from = 'Sweden')")
;(defquery get-persom "match (a:Person) where NOT a.name = 'Emil' AND a.klout < 100 AND NOT (a.klout > 50 OR a.from = 'Sweden')")

;(pprint (create-rule "match (a) WHERE a:Person AND a.condition = \"ASd\" AND a.salary > 10"))

(def db (init-db))

(pprint db)

(get-persons db)

;(pprint db)
;;;
;(print-last-version db)

;(with-session [session (get-session local_db)]
;              (get-persons session))

;(with-session [session (get-session (connect "db"))]
;              (run-query session get-persons)
;              (run-query session  create-person))


;(defquery test-query "create (:Person :Manager {:name \"Emil\", :from \"Sweden\", :klout 99}) - [:FRIEND {:duration \"Forever\"}] -> (:Person :Director {:name \"Frank\", :from \"USA\"})")

;(test-query 1)

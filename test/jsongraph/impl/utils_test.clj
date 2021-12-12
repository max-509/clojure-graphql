(ns jsongraph.impl.utils-test
  (:require [clojure.test :refer :all]
            [clojure.set :refer :all]
            [jsongraph.impl.utils :refer :all]))

(def json-0 {:A 'a :B 'b :C 'c})
(def json-1 {:A 'a :B 'b :C 'c :D 'd})
(def json-2 {:E 'e :F 'f :G 'g})
(def json-3 {:A 'a :B 'b :C 'c :D 'd :E 'e :F 'f :G 'g})

(deftest get-some-test
  (testing "get-key")
  (is (= (get-key json-1 0) :A))
  (is (= (get-key json-1) (get-key json-1 0)))
  (is (= (get-key json-1 3) :D))

  (testing "get-key")
  (is (= (get-val json-1 0) 'a))
  (is (= (get-val json-1) (get-val json-1 0)))
  (is (not= (get-val json-1 2) 'a))

  (testing "get-item")
  (is (= (get-items json-1 :A) {:A 'a}))
  (is (= (get-items json-1 :A :D) {:A 'a :D 'd}))
  (is (not= (get-items json-1 :B) {:C 'c}))
  )

(deftest add-delete-test
  (testing "add-items")
  (is (= (add-items json-1 json-2) json-3))
  (testing "add-empty-items")
  (is (= (add-items {} {}) {}))
  (testing "delete-items")
  (is (= (delete-items json-3 [:C :D :E]) {:A 'a :B 'b :F 'f :G 'g}))

  )

(deftest assoc-items-test
    (println (assoc-items (list [:C {'c 1}] [:B {'b 1}] [:D {'d 1}] [:A {'a 1}] [:A {'h 1}])))
)

(deftest json-difference-test
    (is (= (json-difference json-3 json-2) json-1))
    (is (= (json-difference json-2 json-2) {}))
    (is (= (json-difference json-0 json-1) nil))

)

(deftest list-difference-test
    (println (subvec? [1 2 3] [2 1]))
)

(deftest equal-test
    (is (lists-equal [1 2 3] [2 1 3]))
    (is (lists-equal [1 2 3 5 8] [1 2 3 5 8]))
    (is (not (lists-equal [1 2 5] [1 3 5])))
)

(deftest -test
    (println (subset? (valsSet json-2) (valsSet json-3)))
)
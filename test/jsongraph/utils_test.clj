(ns jsongraph.utils-test
  (:require [clojure.test :refer :all]
            [jsongraph.utils :refer :all]
            [clojure.set :refer :all]))

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
  (is (= (get-items json-1 :A :K) {:A 'a}))
  (is (= (get-items json-1 :D) {:D 'd}))
  (is (not= (get-items json-1 :B) {:C 'c}))
  )

(deftest add-delete-test
  (testing "add-items")
  (is (= (add-items json-1 json-2) json-3))
  (testing "delete-items")
  (is (= (delete-items json-3 [:C :D :E]) {:A 'a :B 'b :F 'f :G 'g}))

  )

(deftest assoc-items-test
    (println (assoc-items (list [:C {'c 1}] [:B {'b 1}] [:D {'d 1}] [:A {'a 1}] [:A {'h 1}])))
)

(deftest json-difference-test
    (println (json-difference json-3 json-2))
)

(deftest list-difference-test
    (println (subvec? [1 2 3] [2 1]))
)

(deftest equal-test
    (println (lists-equal [1 2 3] [2 1 3]))
)

(deftest -test
    (println (subset? (.valSet json-2) (.valSet json-3)))
)
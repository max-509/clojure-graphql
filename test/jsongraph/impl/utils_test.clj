(ns jsongraph.impl.utils-test
  (:require [clojure.test :refer :all]
            [clojure.set :refer :all]
            [jsongraph.impl.utils :refer :all]))

(use '[criterium.core :refer [bench]])

(def arraymap (array-map :f 1 :g 2 :h 4 :y 5 :w 4))
(def hashmap (hash-map :f 1 :g 2 :h 4 :y 5 :w 4))
(defn add-2-keys [m]
  (assoc m :new 2 :w 4))

(defn access-all-keys [m]
  (mapv m [:f :g :h :y :w :not-there]))

(deftest test-performance

; Modification
(bench (add-2-keys arraymap))

(bench (add-2-keys hashmap))

; Access
(bench (access-all-keys arraymap))

(bench (access-all-keys hashmap))

  )



(def json-0 {:A 'a :B 'b :C 'c})
(def json-1 {:A 'a :B 'b :C 'c :D 'd})
(def json-2 {:E 'e :F 'f :G 'g})
(def json-3 {:A 'a :B 'b :C 'c :D 'd :E 'e :F 'f :G 'g})

(def adj {:A [:B :C] :B [:A] :C [:A :D :B]})

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
  (is (= (get-item json-1 :A) {:A 'a}))
  (is (= (get-items json-1 :A :D) {:A 'a :D 'd}))
  (is (not= (get-item json-1 :B) {:C 'c}))
  )

(deftest add-delete-filter-test
  (testing "add-items")
  (is (= (add-items json-1 json-2) json-3))
  (testing "add-empty-items")
  (is (= (add-items {} {}) {}))
  (testing "delete-items")
  (is (= (delete-items json-3 [:C :D :E]) {:A 'a :B 'b :F 'f :G 'g}))
  (testing "filter-nils")
  (is (= (filter-nil {:A 'a :B nil :F 'f}) {:A 'a :F 'f}))
  )

(deftest assoc-items-test
    (is (assoc-items
          (list [:C {'c 1}] [:B {'b 1}] [:D {'d 1}] [:A {'a 1}] [:A {'h 1}] [:A {'a 2}]))
        {:C {'c 1}, :B {'b 1}, :D {'d 1}, :A {'a 2, 'h 1}}))

(deftest conj-key-in-test
  (is (conj-key-in-vals adj)
      {:A ((:A :B) (:A :C))
       :B ((:B :A))
       :C ((:C :A) (:C :D) (:C :B))}))

(deftest json-difference-test-demo
    (is (= (json-difference json-3 json-2) json-1))
    (is (= (json-difference json-2 json-2) {}))
    (is (= (json-difference json-0 json-1) nil))

    (println (json-difference {:A [1 2 4] :B {:A [1 9 4] :B {:A [14] :B {} :C nil} :C 7} :C 8} {:A [1 2 4] :B {:A [1 2 4] :B {:A [14] :B {} :C nil} :C 7}})))

(deftest list-difference-test
    (is (list-difference nil [1 3]) nil))

(deftest subvec-test
    (is (subvec? [2 1] [2 1 3]))
    (is (not (subvec? [6 7] [2 1 3])))
    (is (not (subvec? [] [2 1 3]))))

(deftest equal-test
    (is (lists-equal [1 2 3] [2 1 3]))
    (is (lists-equal [1 2 3 5 8] [1 2 3 5 8]))
    (is (not (lists-equal [1 2 5] [1 3 5]))))

(deftest -test
    (println (keys (transient {:A 'a})))
)
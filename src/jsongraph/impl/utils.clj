(ns jsongraph.impl.utils
  (:require
    [clojure.data.json :as json]
    [clojure.set :refer :all]
    )
  )

(comment How work array-map and hash-map
  An array-map maintains the insertion order of the keys.
  Look up is linear, which is not a problem for small maps (say less than 10 keys).
  If your map is large, you should use hash-map instead. !!!

  Using assoc we get similar results.  9 or fewer items yields an array
;; map.  10 or more yields a hash map.
user=> (type (assoc (make-map 9) :x 1))  ; 10 items -> hash map.
;; => clojure.lang.PersistentHashMap
user=> (type (assoc (make-map 8) :x 1))  ; 9 items -> array map.
;; => clojure.lang.PersistentArrayMap
user=> (type (assoc (make-map 8) :x 1 :y 2))  ; 10 items -> hash map.
;; => clojure.lang.PersistentHashMap

         ;When we use { and } to create a map, the cutoff seems to move to "8.5".
  A map with 9 items created with assoc or zipmap would be an array map,
         ; but a map with 9 items created by { } is a hash map.
  user=> (type {0 0, 1 1, 2 2, 3 3, 4 4, 5 5, 6 6, 7 7})  ; 8 items -> array map.
      => clojure.lang.PersistentArrayMap
  user=> (type {0 0, 1 1, 2 2, 3 3, 4 4, 5 5, 6 6, 7 7, 8 8})  ; 9 items -> hash
      => clojure.lang.PersistentHashMap
  )

(defn print-and-pass- [& args]
  (do
    (doseq [x args] (println x))
    args
    )
  )

(defn print-and-pass [x]
  (do (json/pprint x) x)
  )

(defn wrap-vec [arg]
  (if (coll? arg) arg (list arg)))

(defn get-key [json-map & [idx]]
  (nth
    (keys json-map)
    (if (some? idx) idx 0)
    )
  )

(defn get-val [json-map & [idx]]
  (nth
    (vals json-map)
    (if (some? idx) idx 0)
    )
  )

(defn keysSet [json]
  (set (keys json))
  )

(defn valsSet [json]
  (set (vals json))
  )

(defn keys-intersection [json-1 json-2]
  (vec (intersection (keysSet json-1) (keysSet json-2)))
  )

(defn get-items [json-map -key & -keys]
  (let [-keys (if -keys (conj -keys -key) (wrap-vec -key))
        d (difference
            (set -keys)
            (.keySet json-map))]
    (if (empty? d)
       (select-keys json-map -keys)
       (throw (Throwable. (str "Keys " (vec d) " not found")))
      )
    )
  )

(defn add-items [json-map items]
  (apply (partial merge json-map) items)
  )
(defn assoc-items [items]
   (loop [items items
          json (transient {})
          ]
      (if-let [item (first items)]
        (recur (rest items) (assoc! json (first item) (merge (json (first item)) (second item))))
        (persistent! json)
      )
   )
)

(defn delete-items [json-map [tag & tags]]
  (apply (partial dissoc json-map) tag tags)
  )

(defn list-difference [list-1 list-2]
   (vec (difference (set list-1) (set list-2)))
  )

(defn subvec? [sub -vec]
    (subset? (set sub) (set -vec))
  )

(defn lists-equal [list-1 list-2]
   (= (set list-1) (set list-2))
  )

(defn keys-equal [json-1 json-2]
   (= (.keySet json-1) (.keySet json-2))
  )

(defn json-difference [json-1 json-2]
  (let [s1 (set json-1) s2 (set json-2)
        s1-s2 (difference s1 s2)]
    (if (and (empty? s1-s2) (< (count s1) (count s2)))
      nil (add-items {} (vec s1-s2)))))


(defn gen-json-by-keys [-keys & -val]
  (loop [json {}
         -keys -keys]
    (if (empty? -keys)
      json
      (recur
        (merge json {(first -keys) -val})
        (rest -keys)
        )
      )
    )
  )

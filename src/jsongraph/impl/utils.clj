(ns jsongraph.impl.utils
  (:require
    [clojure.data.json :as json]
    [clojure.set :refer :all]
    [clojure.set :as S]
    )
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
   (vec (S/difference (set list-1) (set list-2)))
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
        s1-s2 (S/difference s1 s2)]
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

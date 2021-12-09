(ns jsongraph.utils
  (:require
    [clojure.data.json :as json]
    [clojure.set :refer :all]
    [clojure.set :as S]
    )
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
(defn -merge-items [items]
  (loop [items (wrap-vec items)
         json (transient {})
         ]
    (println json "\n" items)
    (if-let [item (first items)]
      (do
        (println (first item) (json (first item)))
        (recur (rest items)
               (merge
                 (json (first item))
                 (second item)
                 )
               ))
      (persistent! json)

    ))
  )

(defn delete-items [json-map [tag & tags]]
  (apply (partial dissoc json-map) tag tags)
  )


(defn json-difference [json-1 json-2]
  (#(if (empty? %) nil (assoc-items %)) (vec (S/difference (set json-1) (set json-2))))
  )


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

(defn print-and-pass- [& args]
  (do
    (doseq [x args] (println x))
    args
    )
  )

(defn print-and-pass [x]
  (do (json/pprint x) x)
  )
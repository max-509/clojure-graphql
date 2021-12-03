(ns jsongraph.utils
  (:require [clojure.data.json :as json]))

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


(defn get-item [json-map -key]
    (apply hash-map (find json-map -key))
  )


(defn add-items [json-map items]
  (apply (partial merge json-map) items)
  )

(defn assoc-items [items]
   (loop [items items
          json {}
          ]
      (if-let [item (first items)]
        (recur (rest items) (assoc json (first item) (merge (json (first item)) (second item))))
        json
      )
   )
)

(defn delete-items  [json-map [tag & tags]]
  (apply (partial dissoc json-map) tag tags)
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
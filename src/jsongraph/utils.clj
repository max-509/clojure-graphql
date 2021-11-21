(ns jsongraph.utils
  )

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

(defn assoc-items
  ([items]
   (loop [[item & items] items
          json {}
          ]
      (if (empty? items)
          (assoc json (first item) (second item))
          (recur items (assoc json (first item) (merge (json (first item)) (second item))))
      )
   )
  )
)

(defn delete-items  [json-map [tag & tags]]
  (apply (partial dissoc json-map) tag tags)
  )


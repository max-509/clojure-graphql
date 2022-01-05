(ns jsongraph.api.graphviz
  (:require [clojure.test :refer :all]
            [clojure.string :refer [join]]
            [jsongraph.impl.utils :refer [get-key get-field split-json concat!]]))


(defn- get-label [node]
  (let [label (first (get-field node :labels))]
    (if (keyword? label) (name label) "data")))

(defn- get-property [node]
    (loop [properties (seq (get-field node :properties))
           graphviz-props []]
      (if-let [prop (first properties)]
        (recur
          (rest properties)
          (conj graphviz-props
                (str
                  "\\" \" (name (first prop)) "\\"\"" : " (last prop))))
        (if (empty? graphviz-props)
          "" (str ":{\\l  " (join "\\l  " graphviz-props) "}")))))


(defn get-graphviz-node [node]
  (str "\t" (name (get-key node))
       " ["
       "label = \""
            (get-label node)
            (get-property node)
       "\"]"))

(defn get-graphviz-edge [idx-source idx-target label property]
  (str "\t" idx-source " -> " idx-target " [ label = \"" label property"\" ]"))

(defn get-graphviz-edges [adj-item]
  (let [idx-source (name (get-key adj-item))]
    (loop [out-edges (split-json (get-field adj-item :out-edges))
           graphviz-edges (transient [])]
      (if (empty? out-edges)
        (persistent! graphviz-edges)
        (recur
          (rest out-edges)
          (conj! graphviz-edges
                 (get-graphviz-edge
                   idx-source (name (get-key (first out-edges)))
                   (get-label (first out-edges)) (get-property (first out-edges)))))))))


(defn graph-to-graphviz [graph]
  (loop [graph (split-json (graph :adjacency))
         nodes (transient []) edges (transient [])]
    (if (empty? graph)
      (str
        "digraph {\n fontname=\"Comic Sans MS\"\n node [ fontsize = 18 color=blue penwidth=2.0 fillcolor=grey style=filled]\n edge [penwidth=1.5 fontsize = 18]\n"

        (join "\n" (persistent! nodes))
        "\n\n"
        (join "\n" (persistent! edges))
        "\n}")
      (recur
        (rest graph)
        (conj! nodes (get-graphviz-node (first graph)))
        (concat! edges (get-graphviz-edges (first graph)))
        ))))

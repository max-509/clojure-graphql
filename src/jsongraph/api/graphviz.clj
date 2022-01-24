(ns jsongraph.api.graphviz
  (:require [clojure.test :refer :all]
            [clojure.string :refer [join]]

            [dorothy.core :refer [dot digraph graph-attrs node-attrs edge-attrs]]
            [dorothy.jvm :refer [show! save!]]
            [jsongraph.impl.utils :refer [get-key get-field split-json concat!]]))


(defn- labels-properties2graphviz-label [labels-properties]
  {:label (str
            (join (:labels labels-properties))
            (if-let [props (seq (:properties labels-properties))]
              (str "\\l{\\l"
                   (join ";\\l" (map (fn [[field val]] (str field " " val))
                                     props))
                   "\\l}")
              ""))})

(defn graph2graphviz [graph]
  (let [graph (seq (graph :adjacency))
        options [(node-attrs {:fontsize  16 :width 0.5
                              :shape     :circle :style :filled
                              :color     :green :penwidth 2.0
                              :fillcolor :white})
                 (edge-attrs {:penwidth 1.5 :fontsize 14})]]
    (->
      (digraph
        (concat options
                (reduce (fn [graphviz-elements [key value]]
                          (let [str-key (if (keyword? key) (name key) (str key))
                                graphviz-node [str-key (labels-properties2graphviz-label value)]
                                graphviz-edges (mapv (fn [[out-node-key edge-value]]
                                                       [str-key (if (keyword? out-node-key) (name out-node-key) (str out-node-key))
                                                        (labels-properties2graphviz-label edge-value)])
                                                     (seq (:out-edges value)))]
                            (concat (conj graphviz-elements graphviz-node) graphviz-edges)))
                        []
                        graph)))
      dot)))

(defn save-graphviz
  ([graph path] (save-graphviz graph path :png))
  ([graph path format] (-> (graph2graphviz graph)
                           (save! path {:format format}))))

(defn show-graphviz [graph]
  (-> (graph2graphviz graph)
      show!))
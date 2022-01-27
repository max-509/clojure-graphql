(ns jsongraph.api.graphviz
  (:require [clojure.test :refer :all]
            [clojure.string :refer [join]]

            [dorothy.core :refer [dot digraph graph-attrs node-attrs edge-attrs]]
            [dorothy.jvm :refer [show! save!]]
            [jsongraph.impl.utils :refer [get-key get-field split-json concat!]]))


(defn- labels-properties2graphviz-label [labels-properties]
  (let [label (labels-properties :labels)
        props (seq (labels-properties :properties))]
    {:label (str
            (join label) (if (and (some? label) (some? props)) " : " "")
            (if (some? props)
              (str "{" (if (some? label) "\\l" "")
                   (join ";\\l" (map (fn [[field val]] (str (name field) " : " val))
                                     props))
                   "}")
              ""))}))

(defn- to-str [key]
  (if (keyword? key) (name key) (str key)))


(def options
  [(node-attrs {:fontsize  16 :width 0.5
                :shape     :circle :style :filled
                :color     :green :penwidth 2.0
                :fillcolor :white})
   (edge-attrs {:penwidth 1.5 :fontsize 14})])


(defn graph2graphviz [graph]
  (let [graph (seq (graph :adjacency))]
    (->
      (digraph
        (concat options
                (reduce (fn [graphviz-elements [key value]]
                          (let [str-key (to-str key)
                                graphviz-node [str-key (labels-properties2graphviz-label value)]
                                graphviz-edges (mapv (fn [[out-node-key edge-value]]
                                                       [str-key (to-str out-node-key)
                                                        (labels-properties2graphviz-label edge-value)])
                                                     (seq (:out-edges value)))]
                            (concat (conj graphviz-elements graphviz-node) graphviz-edges)))
                        []
                        graph)))
      dot)))

(def path-to-images "./resources/images/")
(defn save-graphviz
  ([graph filename] (save-graphviz graph filename :png))
  ([graph filename format]
   (println "save to:" (str path-to-images filename))
   (-> (graph2graphviz graph)
       (save! (str path-to-images filename) {:format format}))))

(defn show-graphviz [graph]
  (-> (graph2graphviz graph)
      show!))
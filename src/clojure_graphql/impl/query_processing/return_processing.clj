(ns clojure-graphql.impl.query-processing.return-processing
  (:require [clojure-graphql.impl.query-extracter :as qextr]))


(defn- union-variables [v1 v2]
  [(first v1)
   (let [v1-val (second v1)
         v2-val (second v2)]
     (mapv (fn [v12]
             (merge-with (fn [v1 v2]
                           (if (map? v1)
                             (into v1 v2)
                             (into [] (distinct (into v1 v2)))))
                         (first v12)
                         (second v12)))
           (map vector v1-val v2-val)))])

(defn- processing-return-params [return-params all-variables]
  (apply merge-with union-variables
         (map (fn [return-param]
                (let [return-param-data (qextr/extract-return-param-data return-param)
                      var-name (:var-name return-param-data)
                      field (:field return-param-data)
                      var (get all-variables var-name)]
                  (if (nil? var)
                    {}
                    (if (nil? field)
                      {var-name var}
                      (let [var-type (first var)
                            var-values (second var)]
                        {var-name [var-type (mapv (fn [var-value]
                                                    (let [labels (:labels var-value)
                                                          properties (:properties var-value)]
                                                      {:labels     labels
                                                       :properties {field (get properties field)}}))
                                                  var-values)]})))))
              return-params)))

(defn return-processing [return-params all-variables]
  (if (= :all return-params)
    all-variables
    (processing-return-params return-params all-variables)))

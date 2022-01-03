(ns clojure-graphql.impl.predicates-extracter)

(defn extract-tokens [predicates]
  (rest predicates))

(defn predicate? [token]
  (= :predicate (first token)))

(defn extract-token-value [token]
  (if (predicate? token)
    (second token)
    (first token)))

(defn brackets-predicates? [predicate]
  (= :brackets-predicates (first predicate)))

(defn extract-predicate-value [predicate]
  (rest predicate))

(defn negation-predicate-value? [predicate-value]
  (= :not-command (first (first predicate-value))))

(defn extract-predicate-expression [predicate-value]
  (if (negation-predicate-value? predicate-value)
    (second predicate-value)
    (first predicate-value)))

(defn extract-check-type [check]
  (first check))

(defn extract-check-value [check]
  (rest check))

(defn extract-field-check-var-name [check-value]
  (second (first check-value)))

(defn extract-field-check-field-name [check-value]
  (second (second check-value)))

(defn extract-field-check-command [check-value]
  (first (nth check-value 2)))

(defn extract-field-check-comp-value [check-value]
  (nth check-value 3))
(defproject clojure-graphql "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 ;query translator
                 [instaparse "1.4.10"]

                 ;graph representation
                 [metosin/jsonista "0.3.5"]                 ;fast save/load json (https://github.com/metosin/jsonista)
                 [org.clojure/data.json "2.4.0"]            ;standard lib
                 [danlentz/clj-uuid "0.1.9"]                ;unique index

                 ]
  :repl-options {:init-ns clojure-graphql.core})

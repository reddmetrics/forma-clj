(ns forma.hadoop.jobs.neighbors-test
  (:use forma.hadoop.jobs.neighbors :reload)
  (:use cascalog.api
        forma.hoptree
        midje.sweet)
  (:require [cascalog.ops :as c]
            [forma.matrix.utils :as util]
            [forma.matrix.walk :as walk]
            [incanter.core :as i]))

(def sm-src [["a" 0 0 20 20]
             ["a" 0 0 21 20]
             ["a" 0 0 20 21]
             ["a" 0 0 21 21]
             ["b" 0 0 20 20]
             ["b" 0 0 21 20]])

(def lg-src [["a" 0 0 19 19 1]
             ["a" 0 0 19 20 1]
             ["a" 0 0 19 21 1]
             ["a" 0 0 19 22 1]
             ["a" 0 0 20 19 1]
             ["a" 0 0 20 20 1]
             ["a" 0 0 20 21 1]
             ["a" 0 0 20 22 1]
             ["a" 0 0 21 19 nil]
             ["a" 0 0 21 20 1]
             ["a" 0 0 21 21 1]
             ["a" 0 0 21 22 1]
             ["a" 0 0 22 19 1]
             ["a" 0 0 22 20 1]
             ["a" 0 0 22 21 1]
             ["a" 0 0 22 22 1]
             ["a" 0 0 23 19 1]
             ["a" 0 0 23 20 1]
             ["a" 0 0 23 21 1]
             ["a" 0 0 23 22 1]
             ["a" 0 0 24 19 1]
             ["a" 0 0 24 20 1]
             ["a" 0 0 24 21 1]
             ["a" 0 0 24 22 1]
             ["b" 0 0 19 19 1]
             ["b" 0 0 19 20 1]
             ["b" 0 0 19 21 1]
             ["b" 0 0 19 22 1]
             ["b" 0 0 20 19 1]
             ["b" 0 0 20 20 1]
             ["b" 0 0 20 21 1]
             ["b" 0 0 20 22 1]
             ["b" 0 0 21 19 1]  
             ["b" 0 0 21 20 1]
             ["b" 0 0 21 21 1]
             ["b" 0 0 21 22 1]
             ["b" 0 0 22 19 1]
             ["b" 0 0 22 20 1]
             ["b" 0 0 22 21 1]
             ["b" 0 0 22 22 1]
             ["b" 0 0 23 19 1]
             ["b" 0 0 23 20 1]
             ["b" 0 0 23 21 1]
             ["b" 0 0 23 22 1]
             ["b" 0 0 24 19 1]
             ["b" 0 0 24 20 1]
             ["b" 0 0 24 21 1]
             ["b" 0 0 24 22 1]])

;; TODO: Convert to Midje-Cascalog tests

(def n-tap (neighbor-src "500" sm-src))
(def w-tap (window-attribute-src "500" sm-src lg-src))
(def filtered-tap (filtered-sample-query "500" sm-src lg-src))

(facts
  (count (first (??- n-tap))) => 16
  (count (first (??- w-tap))) => 31)

(fact
  (let [res (??- filtered-tap)]
    (first res) => '(["a" 1728020 8.0]
                     ["a" 1728021 8.0]
                     ["a" 1814420 9.0]
                     ["a" 1814421 9.0]
                     ["b" 1728020 9.0]
                     ["b" 1728021 9.0])))

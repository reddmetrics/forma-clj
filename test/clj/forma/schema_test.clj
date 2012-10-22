(ns forma.schema-test
  "Tests for most functions in forma.schema.  So far, the tests for
  the fundamental, building-block functions are indirectly tested
  through the tests of the higher-order functions (which call the
  lower-order functions)."
  (:use forma.schema
        [midje sweet cascalog])
  (:require [forma.date-time :as date]
            [forma.thrift :as thrift])
  (:import [forma.schema
            ArrayValue DataChunk DataValue DoubleArray FireArray
            FireValue FormaValue IntArray LocationProperty
            LocationPropertyValue LongArray ModisChunkLocation
            ModisPixelLocation ShortArray TimeSeries FormaArray
            NeighborValue]
           [org.apache.thrift TBase TUnion]))

(defn- f-series
  "Returns a fire series of length 10 with starting index `start-idx`
  for testing."
  [start-idx]
  (thrift/TimeSeries* start-idx (repeat 10 (thrift/FireValue* 0 0 0 0))))

(fact "boundaries testing."
  (boundaries [ 0 [1 2 3 4] 1 [2 3 4 5]]) => [1 4]
  (boundaries [ 0 [1 2 3 4] [2 3 4 5]]) => (throws AssertionError))

(tabular
 (fact "sequence adjustment testing."
   (adjust ?a0 ?a-vec ?b0 ?b-vec) => [?start ?a ?b])
 ?a0 ?a-vec    ?b0 ?b-vec    ?start ?a      ?b
 0   [1 2 3 4] 1   [2 3 4 5] 1      [2 3 4] [2 3 4]
 2   [9 8 7]   0   [1 2 3 4] 2      [9 8]   [3 4]
 10  [2 3 4]   1   [1 2 3]   10     []      [])

(facts
  "Test that fire sequence is appropriately trimmed by
 `adjust-fires`.  Note that the interval length defined by :est-start
 and :est-end amounts to 5 periods."

  ;; Test for appropriate trimming over both ends of the interval
  (let [est-map {:est-start "2005-12-31" :est-end "2006-03-01" :t-res "16"}
        [_ _ arr] (apply thrift/unpack (adjust-fires est-map (f-series 826)))]
    (count (thrift/unpack arr)) => 5)

  ;; Test for overhang only over the tail-end of the interval
  (let [est-map {:est-start "2005-12-31" :est-end "2006-03-01" :t-res "16"}
        [_ _ arr] (apply thrift/unpack (adjust-fires est-map (f-series 830)))]
    (count (thrift/unpack arr)) => 2))

(def good-neighbor
  (neighbor-value (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) 3. 4. 5. 6.)))

(def bad-neighbor
  (neighbor-value (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) -9999.0 4. -9999.0 6.)))

(def good-forma
  (thrift/FormaValue* (thrift/FireValue* 1 1 1 1) 1. 2. 3. 4.))

(def bad-forma
  (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) -9999.0 4. -9999.0 6.))

(facts
  "Check `merge-neighbors`"
  (merge-neighbors -9999.0 bad-neighbor bad-neighbor) => empty-neighbor-val
  (merge-neighbors -9999.0 bad-neighbor good-forma) => (neighbor-value good-forma)
  (merge-neighbors -9999.0 good-neighbor bad-forma) => good-neighbor
  (merge-neighbors -9999.0 good-neighbor good-forma)
  => (neighbor-value (thrift/FireValue* 3 2 2 3) 2 2. 1. 3. 2. 4. 3. 5. 4.))

(def neighbors
  "Create a small vector of FormaValues indicating that they are
  neighbors; used for testing that the neighbor values are
  appropriately merged and combined."
  [(thrift/FormaValue* (thrift/FireValue* 2 1 1 2) -9999.0 4. -9999.0 6.)
   (thrift/FormaValue* (thrift/FireValue* 1 1 1 1) 1. 2. 3. 4.)
   (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) 2. 3. 4. 5.)
   (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) 3. 4. -9999.0 6.)])

(def good-neighbor
  (neighbor-value (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) 3. 4. 5. 6.)))

(def bad-neighbor
  (neighbor-value (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) -9999.0 4. -9999.0 6.)))

(def good-forma
  (thrift/FormaValue* (thrift/FireValue* 1 1 1 1) 1. 2. 3. 4.))

(def bad-forma
  (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) -9999.0 4. -9999.0 6.))

(facts
  "Check `merge-neighbors`"
  (merge-neighbors -9999.0 bad-neighbor bad-neighbor) => empty-neighbor-val
  (merge-neighbors -9999.0 bad-neighbor good-forma) => (neighbor-value good-forma)
  (merge-neighbors -9999.0 good-neighbor bad-forma) => good-neighbor
  (merge-neighbors -9999.0 good-neighbor good-forma)
  => (neighbor-value (thrift/FireValue* 3 2 2 3) 2 2. 1. 3. 2. 4. 3. 5. 4.))

(facts
  "Checks that `combine-neighbors` functions properly,
   even with a `nodata` value embedded in the neighbor values."
  (let [nodata -9999.0]
    (combine-neighbors nodata neighbors))
  => (neighbor-value (thrift/FireValue* 3 2 2 3) 2
                                                 1.5 1.
                                                 2.5 2.
                                                 3.5 3.
                                                 4.5 4.)
  (let [nodata -9999.0
        neighbors [(thrift/FormaValue* (thrift/FireValue* 2 1 1 2) nodata 4. nodata 6.)
                   (thrift/FormaValue* (thrift/FireValue* 2 1 1 2) nodata 4. nodata 6.)]]
    (combine-neighbors nodata neighbors)) => empty-neighbor-val)

(fact
  "Check that `forma-seq-prep` correctly handles `nil` at head of timeseries and in the middle."
  (let [nodata -9999.0
        fire-1 (thrift/FireValue* 1 1 1 1)
        fires (thrift/TimeSeries* 826 (repeat 5 fire-1))
        shorts  [nil 1. nil 3. 4.]
        longs   [1. 3. nil 7. 9.]
        t-stats [2. 4. nil 8. 10.]
        breaks  [10. 11. nil 14. 13.]]
    (forma-seq-prep nodata fires shorts longs t-stats breaks)) =>
    [[(thrift/FireValue* 1 1 1 1)
      (thrift/FireValue* 1 1 1 1)
      (thrift/FireValue* 1 1 1 1)
      (thrift/FireValue* 1 1 1 1)
      (thrift/FireValue* 1 1 1 1)]
     [-9999.0 1.0 1.0 1.0 1.0]
     [1.0 3.0 -9999.0 7.0 9.0]
     [2.0 4.0 -9999.0 8.0 10.0]
     [10.0 11.0 11.0 14.0 14.0]])

(fact "Test that the first element of each of the supplied timeseries
  to `forma-seq` is appropriately bundled into the first FormaValue
  of the output timeseries (consisting of FormaValues for each
  period)."
  (let [nodata -9999.0
        fire-1 (thrift/FireValue* 1 1 1 1)
        fire-0 (thrift/FireValue* 0 0 0 0)
        fires (thrift/TimeSeries* 826 (repeat 5 fire-1))
        shorts  [nil 1. nil 3. 4.]
        longs   [1. 3. nil 7. 9.]
        t-stats [2. 4. nil 8. 10.]
        breaks  [10. 11. nil 14. 13.]]
    
    ;; Test for an existing fire
        (ffirst
         (forma-seq nodata fires shorts longs t-stats breaks))
    => (thrift/FormaValue* fire-1 nodata 1. 2. 10.)

    ;; check that second FormaValue is correct
    (second
     (first
      (forma-seq nodata fires shorts longs t-stats breaks)))
    => (thrift/FormaValue* fire-1 1. 3. 4. 11.)

    ;; check that third FormaValue is correct
    (nth
     (first
      (forma-seq nodata fires shorts longs t-stats breaks)) 2)
    => (thrift/FormaValue* fire-1 1. nodata nodata 11.)
     
    ;; Test for the case when there are no fires
    (ffirst
     (forma-seq nodata nil shorts longs t-stats breaks))
    => (thrift/FormaValue* fire-0 nodata 1. 2. 10.)))

(ns forma.thrift-test
  "Unit test coverage for the thrift namespace. In particular, check creating,
  accessing, and unpacking Thrift objects defined in the dev/forma.thrift IDL."
  (:use forma.thrift
        [midje sweet cascalog])
  (:import [forma.schema
            ArrayValue DataChunk DataValue DoubleArray FireArray
            FireValue FormaValue IntArray LocationProperty
            LocationPropertyValue LongArray ModisChunkLocation
            ModisPixelLocation ShortArray TimeSeries FormaArray
            NeighborValue Pedigree]
           [org.apache.thrift TBase TUnion]
           [java.util ArrayList]))

(fact "Check creating and unpacking NeighborValue objects."
  (NeighborValue* (FireValue. 1 1 1 1) 1 2.0 3.0 4.0 5.0 6.0 7.0) =>
  (NeighborValue. (FireValue. 1 1 1 1) 1 2.0 3.0 4.0 5.0 6.0 7.0)
  
  (unpack (NeighborValue. (FireValue. 1 1 1 1) 1 2.0 3.0 4.0 5.0 6.0 7.0)) =>
  [(FireValue. 1 1 1 1) 1 2.0 3.0 4.0 5.0 6.0 7.0 0.0 0.0]

  (let [x (NeighborValue. (FireValue. 1 1 1 1) 1 2.0 3.0 4.0 5.0 6.0 7.0)]
    (doto x
      (.setAvgParamBreak 8.0)
      (.setMinParamBreak 9.0))
    (NeighborValue* (FireValue. 1 1 1 1) 1 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0) => x))

(fact "Check creating and unpacking FireValue objects."
  (FireValue* 1 1 1 1) => (FireValue. 1 1 1 1)
  (unpack (FireValue. 1 1 1 1)) => [1 1 1 1])

(fact "Check creating and unpacking ModisChunkLocation objects."
  (ModisChunkLocation* "500" 1 1 1 1) => (ModisChunkLocation. "500" 1 1 1 1)
  (ModisChunkLocation* "500" 1.0 1 1 1) => (throws AssertionError)
  (ModisChunkLocation* "500" "1" 1 1 1) => (throws AssertionError)
  (unpack (ModisChunkLocation* "500" 1 1 1 1)) => ["500" 1 1 1 1])

(fact "Check creating and unpacking ModisPixelLocation objects."
  (ModisPixelLocation* "500" 1 1 1 1) => (ModisPixelLocation. "500" 1 1 1 1)
  (ModisPixelLocation* "500" 1.0 1 1 1) => (throws AssertionError)
  (ModisPixelLocation* "500" "1" 1 1 1) => (throws AssertionError)
  (unpack (ModisPixelLocation* "500" 1 1 1 1)) => ["500" 1 1 1 1])

(fact "Check unpacking DataValue with primitive value."
  (unpack (DataValue/intVal 1)) => 1
  (unpack (DataValue/doubleVal 1)) => 1.0)

(fact "Check creating and unpacking TimeSeries objects."
  (TimeSeries* 0 1 [1 1 1 1]) =>
  (TimeSeries. 0 1 (->> (vec (map int [1 1 1 1])) IntArray. ArrayValue/ints))

  (unpack (TimeSeries* 0 1 [1 1 1 1])) =>
  [0 1 (->> (map int [1 1 1 1]) IntArray. ArrayValue/ints)])

(fact "Check creating and unpacking FormaValue objects."
  (FormaValue* (FireValue. 1 1 1 1) 1.0 2.0 3.0 4.0) =>
  (FormaValue. (FireValue. 1 1 1 1) 1.0 2.0 3.0 4.0)

  (unpack (FormaValue* (FireValue. 1 1 1 1) 1.0 2.0 3.0 4.0)) =>
  [(FireValue. 1 1 1 1) 1.0 2.0 3.0 4.0])

(fact "Check creating and unpacking DataChunk objects."
  (let [loc (->> (ModisChunkLocation. "500" 8 0 100 24000)
                 LocationPropertyValue/chunkLocation
                 LocationProperty.)
        data (->> (vec (map int [1 1 1 1])) IntArray. ArrayValue/ints DataValue/vals)
        x (DataChunk. "name" loc data "16")
        c (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :date "2001" :pedigree 1)]
    (doto x
      (.setDate "2001")
      (.setPedigree (Pedigree. 1)))
    c => x
    (unpack c) => ["name" loc data "16" "2001" (Pedigree. 1)]))

(fact "Test DataChunk* with various date values."
  (let [loc (->> (ModisChunkLocation. "500" 8 0 100 24000)
                 LocationPropertyValue/chunkLocation
                 LocationProperty.)
        data (->> (vec (map int [1 1 1 1])) IntArray. ArrayValue/ints DataValue/vals)
        dc (DataChunk. "name" loc data "16")]
    (doto dc
      (.setDate "")
      (.setPedigree (Pedigree. 1)))

    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :pedigree 1)
    => (-> (DataChunk. "name" loc data "16")
           (doto)
           (.setPedigree (Pedigree. 1)))

    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :date nil :pedigree 1)
    => (-> (DataChunk. "name" loc data "16")
           (doto)
           (.setPedigree (Pedigree. 1)))
    
    (doto dc
      (.setDate nil))
    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :date nil :pedigree 1) => dc

    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :pedigree 1) => dc
    
    (doto dc
      (.setDate ""))
    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :date "" :pedigree 1) => dc

    (doto dc
      (.setDate "1977"))
    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :date "1977" :pedigree 1) => dc

    (doto dc
      (.setDate nil)
      (.setPedigree (Pedigree. 200000)))
    (DataChunk* "name" (ModisChunkLocation. "500" 8 0 100 24000) [1 1 1 1] "16" :pedigree 200000) => dc))

(facts
  "Check `objs-contains-nodata?"
  (obj-contains-nodata? -9999. (FormaValue*
                                (FireValue* 1 1 1 1)
                                -9999. 1. 1. 1.)) => true
  (obj-contains-nodata? -9999. (FormaValue*
                                (FireValue* 1 1 1 1)
                                  1. 1. 1. 1.)) => false)

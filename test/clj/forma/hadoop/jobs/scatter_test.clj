(ns forma.hadoop.jobs.scatter-test
  (:use cascalog.api
        forma.hadoop.jobs.scatter
        [midje sweet cascalog]
        [forma.thrift :as thrift]))

;; Rain Testing

(def test-gadm
  [["1000" 1 1 1 1 5]
   ["1000" 1 1 1 2 5]
   ["1000" 1 1 1 3 5]
   ["1000" 1 1 1 4 5]
   ["1000" 1 1 1 5 5]
   ["1000" 1 1 1 7 6]
   ["1000" 1 1 1 8 6]
   ["1000" 1 1 1 9 6]])

(def test-rain
  [[1 1 1 1 "2005-12-01" 12.0]
   [1 1 1 2 "2005-12-01" 2.0]
   [1 1 1 3 "2005-12-01" 13.0]
   [1 1 1 4 "2005-12-01" 14.0]
   [1 1 1 5 "2005-12-01" 15.0]
   [1 1 1 2 "2006-01-01" 15.0]
   [1 1 1 2 "2006-02-01" 16.0]
   [1 1 1 2 "2006-03-01" 16.0]
   [1 1 1 8 "2006-03-01" 16.0]])

(let [results [[5 "2005-12-01" 11.2]
               [5 "2006-01-01" 15.0]
               [5 "2006-02-01" 16.0]
               [5 "2006-03-01" 16.0]
               [6 "2006-03-01" 16.0]]]
  (future-fact?- "This now fails, since static-tap is gone. fix!"
                 results
                 (run-rain ..gadm-src.. ..rain-src..)
                 (provided
                   (static-tap ..gadm-src..) => test-gadm
                   (rain-tap ..rain-src..) => test-rain)))

(fact
  "Test for `map-round`"
  (map-round (thrift/TimeSeries* 0 3 [1.1 2.6 3.4 4.0])) => [0 [1 3 3 4]])

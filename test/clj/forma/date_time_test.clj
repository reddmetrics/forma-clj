(ns forma.date-time-test
  (:use [forma.date-time] :reload)
  (:use midje.sweet
        [clj-time.core :only (now date-time month interval)]))

(facts "Date parsing and conversion tests."
  (parse "2011-06-23" :year-month-day) => (date-time 2011 6 23)
  (parse "20110623" :basic-date) => (date-time 2011 6 23)
  (convert "2011-06-23" :year-month-day :basic-date) => "20110623")

(facts "within-dates? test."
  (within-dates? "2005-12-01" "2011-01-02" "2011-01-01") => true
  (within-dates? "20051201" "20110101" "20110102" :format :basic-date) => false

  "within-dates? can't accept datestrings that don't match the default
or supplied formats."
  (within-dates? "2005" "2011" "2010") => (throws IllegalArgumentException))

(tabular
 (fact "Ordinal date examples."
   (ordinal (apply date-time ?pieces)) => ?day)
 ?pieces ?day
 [2005 1 12] 11
 [2005 4 29] 118
 [2005 12 31] 364)

(tabular
 (fact "Ordinal date index should respect leap years."
   (ordinal (apply date-time ?pieces)) => ?day)
 ?pieces ?day
 [2008 3 1] 60
 [2009 3 1] 59)

(facts "Delta testing."  
  (delta #(* % %) 3 4) => 7
  (delta ordinal
         (date-time 2005 01 12)
         (date-time 2005 01 15)) => 3)

(tabular
 (fact "16 and 8 day periods per year, and 1 month periods per year."
   (per-year ?unit ?length) => ?days-per)
?unit    ?length ?days-per
ordinal  16      23
ordinal  8       46
month    1       12)

(fact (periodize "32" (date-time 2005 12 04)) => 431)

(tabular
 (facts "Date conversion, forward and backward, in various temporal
 resolutions."
   (datetime->period ?res ?date) => ?period
   (period->datetime ?res ?period) => (beginning ?res ?date))
 ?res ?date        ?period
 "32" "2005-12-04" 431
 "32" "2006-01-01" 432
 "16" "2005-12-04" 826
 "16" "2003-04-22" 765
 "16" "2003-04-23" 766
 "16" "2003-12-31" 781
 "8"  "2003-04-12" 1530
 "8"  "2003-12-31" 1563)

(facts "Beginning tests!"
  (beginning "16" "2005-12-31") => "2005-12-19"
  (beginning "32" "2005-12-31") => "2005-12-01"
  (beginning "32" "2011-06-23T22" :date-hour) => "2011-06-01T00")

(facts "diff-in-days tests"
  "Dates properly span years..."
  (diff-in-days "2011-12-30" "2012-01-02") => 3

  "And months."
  (diff-in-days "2011-11-01" "2012-01-02") => 62

  "The two dates must be provided in increasing order."
  (diff-in-days "2012-01-02" "2011-12-30") => (throws IllegalArgumentException))

(facts "date-offset tests"
  (date-offset "16" 1 "32" 1) => 15)

(facts "period-span tests."
  "December of the second year."
  (period-span "32" 12) => 31

  "Checking that we get proper month lengths for all of 1970 (first 12
   months since the epoch)."
  (for [month (range 12)]
    (period-span "32" month)) =>  [31 28 31 30 31 30 31 31 30 31 30 31])

(facts "shift-resolution tests"
  (shift-resolution "32" "16" 1) => 1
  (shift-resolution "32" "16" 10) => 19)

(facts "current-period tests."
  (current-period "32") => 431
  (current-period "16") => 825

  (against-background
    (now) => (date-time 2005 12 01 13 10 9 128)))

(fact "Relative period test."
  (relative-period "32" 391 ["2005-02-01" "2005-03-01"]) => [30 31])

(fact "Millisecond tests."
  (msecs-from-epoch (date-time 2002 12)) => 1038700800000
  (monthly-msec-range (date-time 2005 11)
                      (date-time 2005 12)) => [1130803200000 1133395200000])

(facts
  "Check that `date-str->vec-idx` outputs the correct index for a date"
  (date-str->vec-idx "16" "2000-01-01" [2 4 6] "2000-01-17") => 1
  (date-str->vec-idx "16" "2000-01-01" [1 2 3] "2001-01-01") => nil)

(facts
  "Check that `get-val-at-date` outputs the correct index for a date"
  (get-val-at-date "16" "2000-01-01" [2 4 6] "2000-01-17") => 4
  (get-val-at-date "16" "2000-01-01" [1 2 3] "2001-01-01") => nil
  (get-val-at-date "16" "2000-01-01" [2 4 6] "2005-01-01" :out-of-bounds-val 5) => 5
  (get-val-at-date "16" "2000-01-01" [2 4 6] "2005-01-01" :out-of-bounds-idx 0) => 2)

(fact "Check key->period."
  (key->period "16" :2005-12-31) => 827
  (key->period "16" :2005-12-19) => 827
  (key->period "16" :2000-02-18) => 693
  (key->period "32" :2005-12-31) => 431
  (key->period "32" :2000-02-18) => 361)

(fact "Check period->key."
  (period->key "16" 827) => :2005-12-19
  (period->key "16" 693) => :2000-02-18
  (period->key "32" 431) => :2005-12-01
  (period->key "32" 361) => :2000-02-01)

(fact "Check key-span."
  (key-span :2005-12-31 :2006-01-18 "16") => [:2005-12-19 :2006-01-01 :2006-01-17])

(fact "Check consecutive?"
  (consecutive? "16" [:2006-01-01 :2006-01-17]) => true
  (consecutive? "16" [:2006-01-01 :2006-01-01]) => false
  (consecutive? "16" [:2006-01-01 :2007-12-31]) => false)

(fact "Check ts-vec->ts-map."
  (ts-vec->ts-map :2005-12-19 "16" [1 2 3])
  => {:2005-12-19 1 :2006-01-01 2 :2006-01-17 3})

(fact "Check ts-map->ts-vec."
  (ts-map->ts-vec "16" {:2005-12-19 1 :2006-01-01 2 :2006-01-17 3} -9999.0) => [1 2 3]
  (ts-map->ts-vec "16" {:2005-12-19 1 :2006-01-01 2 :2006-02-02 3} -9999.0) => [1 2 -9999.0 3]
  (ts-map->ts-vec "16" {:2005-12-19 1 :2006-01-01 2 :2006-02-02 3} -9999.0 :consecutive true) => (throws AssertionError))

(fact "Check get-ts-map-start-idx"
  (get-ts-map-start-idx "16" {:2005-12-19 1 :2000-02-18 47}) => 693)

(tabular
 (fact "Check ts-vec->ts-map"
   (ts-vec->ts-map ?date ?t-res ?coll) => ?result)
 ?date ?t-res ?coll ?result
 :2012-01-01 "16" [1 2 3] {:2012-01-01 1 :2012-01-17 2 :2012-02-02 3}
 :2012-01-01 "16" [3 2 1] {:2012-01-01 3 :2012-01-17 2 :2012-02-02 1})

(tabular
 (fact "Check ts-map->ts-vec"
   (ts-map->ts-vec ?t-res ?m ?nodata) => ?result)
 ?t-res ?m ?nodata ?result
 "16" {:2012-01-01 1 :2012-01-17 2} -9999.0 [1 2]
 "16" {:2012-01-01 1 :2012-02-02 2} -9999.0 [1 -9999.0 2]
 "32" {:2012-01-01 1 :2012-02-01 2} -9999.0 [1 2]
 "32" {:2012-01-01 1 :2012-03-01 2} -9999.0 [1 -9999.0 2])

(fact "Check inc-date"
  (inc-date "2000-01-01") => "2000-01-02"
  (inc-date "2000-01-01" 10) => "2000-01-11")

(fact "Check date-str->millis"
  (date-str->millis "2000-01-01")
  => (msecs-from-epoch (parse "2000-01-01" :year-month-day))
  (date-str->millis ["2000-01-01"]) => (throws AssertionError)
    (date-str->millis 1) => (throws AssertionError))

(fact "Check sorted-dates?"
  (sorted-dates? "2005-01-01" "2005-01-01") => true
  (sorted-dates? "2005-01-01" "2004-01-01") => false
  (sorted-dates? ["2005-01-01" "2005-01-01"]) => true
  (sorted-dates? ["2005-01-01" "2005-01-01" "2006-01-01"]) => true
  (sorted-dates? ["2005-01-01" "2006-01-01" "2007-01-01"]) => true
  (sorted-dates? ["2005-01-01" "2004-01-01"]) => false
  (sorted-dates? ["2005-01-01" "2004-01-01" "2005-01-01"]) => false
  (sorted-dates? [1 "2005-01-01"]) => (throws AssertionError)
  (sorted-dates? 1 2) => (throws AssertionError))

(facts "Check `date-intervals-overlap?`"
  (date-intervals-overlap? "2006-06-01" "2006-08-01" "2006-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2006-06-01" "2006-08-01") => true
  (date-intervals-overlap? "2006-01-01" "2006-01-01" "2006-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2006-01-01" "2006-01-01") => true
  (date-intervals-overlap? "2007-01-01" "2007-01-01" "2006-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2007-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2005-01-01" "2007-01-01" "2006-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2005-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2005-01-01" "2008-01-01" "2006-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2005-01-01" "2008-01-01") => true
  (date-intervals-overlap? "2005-01-01" "2008-01-01" "2006-01-01" "2007-01-01") => true
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2005-01-01" "2008-01-01") => true
  (date-intervals-overlap? "2005-01-01" "2008-01-01" "2000-01-01" "2001-01-01") => false
  (date-intervals-overlap? "2000-01-01" "2001-01-01" "2005-01-01" "2008-01-01") => false
  (date-intervals-overlap? "2010-01-01" "2000-01-01" "2006-01-01" "2007-01-01")
  => (throws AssertionError)
  (date-intervals-overlap? "2006-01-01" "2007-01-01" "2010-01-01" "2000-01-01")
  => (throws AssertionError))

(tabular
 (fact "Check `period-intervals-overlap?`"
   (let [t-res "16"]
     (period-intervals-overlap? ?start1 ?end1 ?start2 ?end2 t-res) => ?result))
 ?start1 ?end1 ?start2 ?end2 ?result
 1 2 3 4 false
 3 4 1 2 false
 1 2 2 4 true
 2 4 1 2 true
 2 1 3 4 (throws AssertionError)
 3 4 2 1 (throws AssertionError)
 -1 1 3 4 (throws AssertionError))

(tabular
 (fact "Check `temporal-subvec`."
   (temporal-subvec ?get-start ?get-end "2005-01-01" "2005-02-18" "16" ?series)
   => ?result)
 ?get-start ?get-end ?series ?result
 "2005-01-01" "2005-02-18" [1 2 3 4] [1 2 3]
 "2005-01-01" "2005-02-02" [1 2 3 4] [1 2]
 "2005-01-01" "2005-01-17" [1 2 3 4] [1]
 "2000-01-01" "2005-02-18" [1 2 3 4] (throws AssertionError) ;; starts early
 "2005-01-01" "2006-02-18" [1 2 3 4] (throws AssertionError) ;; ends late
 "2000-01-01" "2010-02-18" [1 2 3 4] (throws AssertionError)) ;; interval too wide

(fact "Check `temporal-subvec*`."
  (temporal-subvec* "2005-01-01" "2005-02-02" "2005-01-01"
                    "2005-02-18" "16" [1 2 3 4] [5 6 7 8]) => [[1 2] [5 6]])

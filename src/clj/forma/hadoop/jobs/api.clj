(ns forma.hadoop.jobs.api
  "We’ll generate an API master dataset after each update. This
  dataset will be vertically partitioned into sequence files using a
  template tap as follows:

  /api/{iso}/{year}"
  (:use cascalog.api
        [forma.source.gadmiso :only (gadm2->iso)]
        [forma.hadoop.jobs.postprocess :only (first-hit)])
  (:require [cascalog.ops :as c]
            [forma.postprocess.output :as o]
            [forma.reproject :as r]
            [forma.date-time :as date]
            [forma.utils :as u]
            [forma.hadoop.predicate :as p]))

(defmapcatop wide->long
  "Convert a series from wide to long form.

   Usage:
     (let [start-idx 827
           src [[1 [1 2 3]]]]
       (??<- [?id ?pd ?val]
             (src ?id ?series)
             (wide->long start-idx ?series :> ?pd ?val)))
     ;=> [[1 827 1] [1 828 2] [1 829 3]]"
  [start-idx series]
  (let [len (count series)
        idxs (range start-idx (inc (+ start-idx len)))]
    (map vector idxs series)))

(defn get-hit-val
  "Get value of the first hit in a time series."
  [thresh series]
  (let [first-hit-idx (first-hit thresh series)]
    (if (nil? first-hit-idx)
      nil
      (nth series first-hit-idx))))

(defn get-hit-period
  "Get the period for the first hit in the time series."
  [start-idx thresh series]
  (let [first-hit-idx (first-hit thresh series)]
    (if (nil? first-hit-idx)
      nil
      (+ start-idx first-hit-idx))))

(defn latest-hit?
  "Check whether series crosses threshold in last period."
  [thresh series]
  (let [last-idx (dec (count series))]
    (-> (first-hit thresh series)
        (= last-idx))))

(defn get-hit-period-and-value
  "Get the period and value of the first hit in a series."
  [start-idx thresh series]
  (let [hit-period (get-hit-period start-idx thresh series)
        hit-val (get-hit-val thresh series)]
    [hit-period hit-val]))

(defn api-prep-query
  [src t-res nodata]
  (<- [?lat ?lon ?iso ?iso-extra ?gadm2 ?start-idx ?clean-series]
      (src ?s-res ?mod-h ?mod-v ?sample ?line ?start-idx ?prob-series ?gadm2)
      (r/modis->latlon ?s-res ?mod-h ?mod-v ?sample ?line :> ?lat ?lon)
      (gadm2->iso ?gadm2 :> ?iso)
      (p/add-fields ?iso :> ?iso-extra)
      (o/clean-probs ?prob-series nodata :> ?clean-series)))

(defn ever-crosses-thresh?
  [thresh series]
  (<= thresh (last series)))

(defn prob-series->long-ts
  "Returns a Cascalog query that that takes output already joined with GADM,
   drops pixels that never meet the threshold, and converts remaining tuples
   to long form according to API spec.

   ?year field is intended for use with template tap. ?iso-extra is
   included because the template tap removes :templatefields
   from :outfields, but we want the iso code included in the output."
  [est-map src & [thresh]]
  (let [thresh (or thresh 0)
        nodata (:nodata est-map)
        est-start (:est-start est-map)
        t-res (:t-res est-map)
        clean-src (api-prep-query src t-res nodata)]
    (<- [?lat ?lon ?iso ?iso-extra ?gadm2 ?date ?year ?prob]
        (clean-src ?lat ?lon ?iso ?iso-extra ?gadm2 ?start-idx ?clean-series)
        (ever-crosses-thresh? thresh ?clean-series)
        (wide->long ?start-idx ?clean-series :> ?period ?prob)
        (date/period->datetime t-res ?period :> ?date)
        (date/convert ?date :year-month-day :year :> ?year))))

(defn prob-series->first-hit
  "Returns a Cascalog query that generates data for the `first-hit`
   API endpoint. Takes a source of FORMA output already joined with
   GADM. Filters out any pixels that do not meet the threshold."
  ([est-map src]
     (let [thresh 0]
       (prob-series->first-hit est-map src thresh)))
  ([est-map src thresh]
     (let [t-res (:t-res est-map)
           nodata (:nodata est-map)
           est-start (:est-start est-map)
           clean-src (api-prep-query src t-res nodata)]
       (<- [?lat ?lon ?iso ?iso-extra ?gadm2 ?date ?prob]
           (clean-src ?lat ?lon ?iso ?iso-extra ?gadm2 ?start-idx ?clean-series)
           (get-hit-period-and-value ?start-idx thresh ?clean-series :> ?period ?prob)
           (date/period->datetime t-res ?period :> ?date)))))

(defn prob-series->latest
  "Returns a Cascalog query that generates data for the `latest`
   API endpoint. Takes a source of FORMA output already joined with
   GADM. Filters out any pixels that do not meet the threshold."
  ([est-map src]
     (let [thresh 0]
       (prob-series->latest est-map src thresh)))
  ([est-map src thresh]
     (let [t-res (:t-res est-map)
           nodata (:nodata est-map)
           est-start (:est-start est-map)
           clean-src (api-prep-query src t-res nodata)]
       (<- [?lat ?lon ?iso ?iso-extra ?gadm2 ?date ?prob]
           (clean-src ?lat ?lon ?iso ?iso-extra ?gadm2 ?start-idx ?clean-series)
           (latest-hit? thresh ?clean-series)
           (get-hit-period-and-value ?start-idx thresh ?clean-series :> ?period ?prob)
           (date/period->datetime t-res ?period :> ?date)))))

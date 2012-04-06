(ns forma.trends.analysis
  (:use [forma.matrix.utils]
        [midje.cascalog]
        [cascalog.api]
        [clojure.math.numeric-tower :only (sqrt floor abs expt)]
        [forma.date-time :as date]
        [clojure.tools.logging :only (error)])
  (:require [forma.utils :as utils]
            [incanter.core :as i]
            [incanter.stats :as s]
            [forma.schema :as schema]))

;; Hansen break statistic

(defn linear-residuals
  "returns the residuals from a linear model; cribbed from
  incanter.stats linear model"
  [y x & {:keys [intercept] :or {intercept true}}]
  (let [_x (if intercept (i/bind-columns (replicate (i/nrow x) 1) x) x)
        xt (i/trans _x)
        xtx (i/mmult xt _x)
        coefs (i/mmult (i/solve xtx) xt y)]
    (i/minus y (i/mmult _x coefs))))

(defn first-order-conditions
  "returns a matrix with residual weighted cofactors (incl. constant
  vector) and deviations from mean; corresponds to first-order
  conditions $f_t$ in the following reference: Hansen, B. (1992)
  Testing for Parameter Instability in Linear Models, Journal for
  Policy Modeling, 14(4), pp. 517-533"
  [coll]
  {:pre [(i/matrix? coll)]}
  (let [X (i/matrix (range (count coll)))
        resid (linear-residuals coll X)
        sq-resid (i/mult resid resid)
        mu (utils/average sq-resid)]
    (i/trans (i/bind-columns
              (i/mult resid X)
              resid
              (i/minus sq-resid mu)))))

(defn hansen-stat
  "returns the Hansen (1992) test statistic, based on (1) the first-order
  conditions, and (2) the cumulative first-order conditions."
  [coll]
  (let [foc (first-order-conditions coll)
        focsum (map i/cumulative-sum foc)
        foc-mat (i/mmult foc (i/trans foc))
        focsum-mat (i/mmult focsum (i/trans focsum))]
    (i/trace
     (i/mmult
      (i/solve (i/mult foc-mat (count coll)))
      focsum-mat))))

;; Long-term trend characteristic; supporting functions 

(defn trend-characteristics
  [y x & {:keys [intercept] :or {intercept true}}]
  (let [_x (if intercept (i/bind-columns (replicate (i/nrow x) 1) x) x)
        xt (i/trans _x)
        xtx (i/mmult xt _x)
        coefs (i/mmult (i/solve xtx) xt y)
        fitted (i/mmult _x coefs)
        resid (i/to-list (i/minus y fitted))
        sse (i/sum-of-squares resid)
        mse (/ sse (- (i/nrow y) (i/ncol _x)))
        coef-var (i/mult mse (i/solve xtx))
        std-errors (i/sqrt (i/diag coef-var))
        t-tests (i/div coefs std-errors)]
    [coefs t-tests]))

(defn long-stats
  "returns a list with both the value and t-statistic for the OLS
  trend coefficient for a time series, conditioning on a variable
  number of cofactors"
  [ts & cofactors]
  (let [time-step (utils/idx ts)
        X (if (empty? cofactors)
            (i/matrix time-step)
            (apply i/bind-columns time-step cofactors))]
    (try (map second (trend-characteristics ts X))
         (catch Throwable e
           (error (str "TIMESERIES ISSUES: " ts ", " cofactors) e)))))

;; Short-term trend characteristic; supporting functions

(defn trend-mat
  "returns a (`len` x 2) matrix, with first column of ones; and second
  column of 1 through `len`"
  [len]
  (i/bind-columns
   (repeat len 1)
   (map inc (range len))))

(defn hat-mat
  "returns hat matrix for a trend cofactor matrix of length
  `block-len`, used to calculate the coefficient vector for ordinary
  least squares"
  [block-len]
  (let [X (trend-mat block-len)]
    (i/mmult (i/solve (i/mmult (i/trans X) X))
             (i/trans X))))

(defn grab-trend
  "returns the trend from an ordinary least squares regression of a
  spectral time series on an intercept and a trend variable"
  [hat-mat sub-coll]
  (second (i/mmult hat-mat sub-coll)))

(defn moving-subvec
  "returns a vector of incanter sub-matrices, offset by 1 and of
  length `window`; works like partition for non-incanter data
  structures."
  [window coll]
  (loop [idx 0
         res []]
    (if (> idx (- (count coll) window))
      res
      (recur
       (inc idx)
       (conj res
             (i/sel coll :rows (range idx (+ window idx))))))))

(defn windowed-trend
  "returns a vector of short-term trend coefficients of block length
  `block-len`"
  [block-len freq spectral-ts reli-ts]
  (map (partial grab-trend (hat-mat block-len))
       (moving-subvec block-len
                      (i/matrix spectral-ts reli-ts))))

;; Collect the long- and short-term trend characteristics

(defn telescoping-long-trend
  "returns a three-tuple with the trend coefficient, trend t-stat, and
  the hansen statistic for each period between `start-idx` (inclusive)
  and `end-idx` (inclusive)."
  [freq start-idx end-idx spectral-ts cofactor-ts]
  (let [cofactor-tele (take (count spectral-ts) cofactor-ts)]
    (map flatten
         (transpose [(map hansen-stat (map i/matrix spectral-ts))
                     (map long-stats spectral-ts cofactor-tele)]))))

(defn telescoping-short-trend
  "returns a vector of the short-term trend coefficients over time
  series blocks of length `long-block`, smoothed by moving average
  window of length `short-block`. The coefficients are calculated
  along the entire time-series, but only apply to periods at and after
  the end of the training period."
  [long-block short-block freq training-end end-idx spectral-ts reli-ts]
  (let [leading-buffer (+ 2 (- training-end (+ long-block short-block)))
        results-len (- (inc end-idx) training-end)]
    (->> (windowed-trend long-block freq spectral-ts reli-ts)
         (utils/moving-average short-block)
         (reductions min)
         (drop (dec leading-buffer))
         (take results-len))))

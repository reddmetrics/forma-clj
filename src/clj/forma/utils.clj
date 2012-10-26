(ns forma.utils
  (:use [clojure.math.numeric-tower :only (round expt)])
  (:require [clojure.java.io :as io]
            [forma.thrift :as thrift])
  (:import  [java.io InputStream]
            [java.util.zip GZIPInputStream]))

;; ## Argument Validation

(defn throw-illegal [s]
  (throw (IllegalArgumentException. s)))

(defn strings->floats
  "Accepts any number of string representations of floats, and
  returns the corresponding sequence of floats."
  [& strings]
  (map #(Float. %) strings))

(defn between?
  "Returns true of the supplied arg `x` falls between the supplied
  `lower` and `upper` bounds (inclusive), false otherwise."
  [lower upper x]
  (and (>= x lower) (<= x upper)))

(defn thrush [& args]
  (reduce #(%2 %1) args))

(defn nth-in
  "Takes a nested collection and a sequence of keys, and returns the
  value obtained by taking `nth` in turn on each level of the nested
  collection."
  [coll ks]
  (apply thrush coll
         (for [k ks] (fn [xs] (nth xs k)))))

(defn unweave
  "Splits a sequence with an even number of entries into two sequences
  by pulling alternating entries.

  Example usage:
    (unweave [0 1 2 3]) => [(0 2) (1 3)]"
  [coll]
  {:pre [(seq coll), (even? (count coll))]}
  [(take-nth 2 coll) (take-nth 2 (rest coll))])

(defn find-first
  "Returns the first item of coll for which (pred item) returns logical true.
  Consumes sequences up to the first match, will consume the entire sequence
  and return nil if no match is found."
  [pred coll]
  (first (filter pred coll)))

(defn scale
  "Returns a collection obtained by scaling each number in `coll` by
  the supplied number `fact`."
  [factor coll]
  (for [x coll] (* x factor)))

(defn multiply-rows
  "multiply matrix rows (in place) by a collection"
  [coll mat]
  (map (partial map * coll) mat))

(defn weighted-mean
  "Accepts a number of `<val, weight>` pairs, and returns the mean of
  all values with corresponding weights applied.  Preconditions ensure
  that there are pairs of value and weights, and that all weights are
  greater than or equal to zero.

  Example usage:
    (weighted-avg 8 3 1 1) => 6.25"
  [& val-weight-pairs]
  {:pre [(even? (count val-weight-pairs))
         (every? #(>= % 0) (take-nth 2 (rest val-weight-pairs)))]}
  (double (->> (for [[x weight] (partition 2 val-weight-pairs)]
                 [(* x weight) weight])
               (reduce (partial map +))
               (apply /))))

(defn positions
  "Returns a lazy sequence containing the positions at which pred
   is true for items in coll."
  [pred coll]
  (for [[idx elt] (map-indexed vector coll) :when (pred elt)] idx))

(defn trim-seq
  "Trims a sequence with initial value indexed at x0 to fit within
  bottom (inclusive) and top (exclusive).

  Example usage: 
    (trim-seq 0 2 0 [4 5 6]) => [4 5]"
  [bottom top x0 seq]
  {:pre [(not (empty? seq))]}
  (->> seq
       (drop (- bottom x0))
       (drop-last (- (+ x0 (count seq)) top))))

(defn windowed-map
  "maps an input function across a sequence of windows onto a vector
  `v` of length `window-len` and offset by 1.

  Note that this does not work with some functions, such as +. Not sure why"
  [f window-len v]
  (pmap f (partition window-len 1 v)))

(defn average [lst] (/ (reduce + lst) (count lst)))

(defn moving-average
  "returns a moving average of windows on `lst` with length `window`"
  [window lst]
  (map average (partition window 1 lst)))

(defn idx
  "return a list of indices starting with 1 equal to the length of
  input"
  [coll]
  (vec (map inc (range (count coll)))))

;; ## IO Utils

(defn input-stream
  "Attempts to coerce the given argument to an InputStream, with
  automatic flipping to `GZipInputStream` if appropriate for the
  supplied input. see `clojure.java.io/input-stream` for guidance on
  valid arguments."
  ([arg] (input-stream arg nil))
  ([arg default-bufsize]
     (let [^InputStream stream (io/input-stream arg)]
       (try
         (.mark stream 0)
         (if default-bufsize
           (GZIPInputStream. stream default-bufsize)
           (GZIPInputStream. stream))
         (catch java.io.IOException e
           (.reset stream)
           stream)))))

;; When using an input-stream to feed lazy-seqs, it becomes difficult
;; to use `with-open`, as we don't know when the application will be
;; finished with the stream. We deal with this by calling close on the
;; stream when our lazy-seq bottoms out.

(defn force-fill!
  "Forces the contents of the supplied `InputStream` into the supplied
  byte buffer. (In certain cases, such as when a GZIPInputStream
  doesn't have a large enough buffer by default, the stream simply
  won't load the requested number of bytes. `force-fill!` blocks until
  `buffer` has been filled."
  [buffer ^InputStream stream]
  (loop [len (count buffer), off 0]
    (let [read (.read stream buffer off len)
          newlen (- len read)
          newoff (+ off read)]
      (cond (neg? read) read
            (zero? newlen) (count buffer)
            :else (recur newlen newoff)))))

(defn partition-stream
  "Generates a lazy seq of byte-arrays of size `n` pulled from the
  supplied input stream `stream`."
  [n ^InputStream stream]
  (let [buf (byte-array n)]
    (if (pos? (force-fill! buf stream))
      (lazy-seq
       (cons buf (partition-stream n stream)))
      (.close stream))))

(defn read-numbers [x]
  (binding [*read-eval* false]
    (let [val (read-string x)]
      (assert (number? val) "You can only liberate numbers!")
      val)))

;; ## Byte Manipulation

(def float-bytes
  (/ ^Integer Float/SIZE
     ^Integer Byte/SIZE))

(def byte-array-type
  (class (make-array Byte/TYPE 0)))

(defn get-replace-vals-locs
  "Search collection for the location of bad values, and find replacement values.
   Replacements are found to the left of a bad value, starting to the immediate
   left of each bad value and ending at the first element of the collection.
   Returns a map of replacement indices and replacement values.

  If there are no good values to the left of a given bad value,
  `default` will be returned for that value.

  Note that the check for bad values is type-dependent because of use of `=`
  with positions. For example, looking for -9999 will not pick up -9999.0."
  [bad-val coll default]
  (let [bad-locs (positions (partial = bad-val) coll)]
    (zipmap bad-locs
            (for [i bad-locs]
                (if (zero? i) ;; avoids out of bounds exception of idx -1
                  default
                  (loop [j i]
                    (cond
                     (not= bad-val (coll j)) (coll j)
                     (zero? j) default ;; all bad values from start to j
                     :else (recur (dec j)))))))))

(defn replace-from-left
  "Replace all instances of `bad-val` in a collection with good
   replacement values, defined as the first good value to the left of
   a given element. The value given with the `:default`
   keyword (defaults to `nil` is used in case a suitable replacement
   cannot be found to the left (e.g. the first or first several
   elements of the vector is \"bad\"). Note that the comparison with the bad
   value is type dependent, and a -9999 will not be replaced if you search
   -9999.0.

   Usage:
     (replace-from-left -9999 [1 -9999 3])
     ;=> (1 nil 3)

     (replace-from-left -9999 [1 -9999 -9999 3])
     ;=> (1 1 1 3)

     (replace-from-left -9999 [1 -9999 3] :default -1)
     ;=> (1 -1 3)

     (replace-from-left -9999 [-9999 -9999 3] :default -1)
     ;=> (-1 -1 3)

     (replace-from-left -9999 [1 -9999 -9999.0 3] :default -1)
     ;=> (1 1 -9999.0 3)

     (replace-from-left -9999 [1 -9999.0 -9999 3] :default -1)
     ;=> (1 -9999.0 -9999.0 3)"
  [bad-val coll & {:keys [default] :or {default nil}}]
  (let [replace-map (get-replace-vals-locs bad-val coll default)]
    (for [i (range (count coll))]
      (if (contains? (set (keys replace-map)) i)
        (get replace-map i)
        (coll i)))))

(defn replace-from-left*
  "Nest `replace-from-left` for use with Cascalog"
  [bad-val coll & {:keys [default] :or {default nil}}]
  [(vec (replace-from-left bad-val coll :default default))])

(defn obj-contains-nodata?
  "Check whether any fields in thrift object contain nodata value"
  [nodata obj]
  (-> obj
      (thrift/unpack)
      (set)
      (contains? nodata)))

(defn filter*
  "Wrapper for `filter` to make it safe for use with vectors in Cascalog.

   Usage:
     (let [src [[1 [2 3 2]] [3 [5 nil 6]]]]
       (??<- [?a ?all-twos]
         (src ?a ?b)
         (filter* (partial = 2) ?b :> ?all-twos)))
     ;=> ([1 [2 2]] [3 []])"
  [pred coll]
  [(vec (filter pred coll))])

(defn replace-all
  "Replace all instances of a value in a collection with a supplied replacement
   value.

  Usage:
    (replace-all nil -9999 [1 nil 3])
    ;=> [1 -9999 3]

    (replace-all -9999 nil [1 -9999 3])
    ;=> [1 nil 3]

    (replace-all -9999.0 nil [1 -9999 3])
    ;=> [1 -9999 3]"
  [to-replace replacement coll]
  (let [idxs (positions (partial = to-replace) coll)]
    (if (empty? idxs)
      coll
      (apply assoc coll (interleave idxs (repeat (count idxs) replacement))))))

(defn replace-all*
  "Wrapper for `replace-all` to make it safe for use with vectors in Cascalog"
  [to-replace replacement coll]
  [(vec (replace-all to-replace replacement coll))])

(defn rest*
  "Wrapper for `rest` is safe for use with Cascalog"
  [coll]
  (vector (vec (rest coll))))


(ns get-args.args-validator)

(defn validate-args
  "Ensure that the resulting parsed args match the spec."
  [spec args]
  (letfn [(is-required? [x] (:required (last x)))]
    (let [required-args (into [] (map first (filter is-required? spec)))]
      (if (clojure.set/subset? (set required-args) (set (keys args))) args nil))))

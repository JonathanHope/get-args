(ns get-args.args-validator)

(defn validate-args
  "Walk through the parsed out arguments and make sure they match the parsed out spec."
  [spec args]
  (let [required-args (into [] (map first (filter (fn [x] (:required (last x))) spec)))]
    (if (clojure.set/subset? (set required-args) (set (keys args))) args nil)))

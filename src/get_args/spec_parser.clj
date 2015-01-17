(ns get-args.spec-parser)

(defn parse-spec
  "Parse the raw spec into a more consumable data structure."
  [spec]
  (loop [parsed-spec {} remaining-spec spec last-keyword nil]
    (if (empty? remaining-spec) parsed-spec
        (let [spec-part (first remaining-spec)]
          (recur (cond (keyword? spec-part) (assoc parsed-spec spec-part {})
                       (vector? spec-part) (assoc-in parsed-spec [last-keyword] (assoc (last-keyword parsed-spec) :aliases spec-part))
                       (map? spec-part) (assoc-in parsed-spec [last-keyword] (merge (last-keyword parsed-spec) spec-part))
                       :else (parsed-spec))
                 (rest remaining-spec)
                 (if (keyword? spec-part) spec-part last-keyword))))))

(ns get-args.args-parser
  (require [reduce-fsm :as fsm]))

(defn parse-args
  "Given a parsed spec parse and validate raw command line arguments."
  [args-spec args]
  
  (letfn [;; Get the key for an args alias.
          (get-arg-key [arg-alias]
            (letfn [(arg-has-alias [x] (-> x
                                           (last)
                                           (:aliases)
                                           (set)
                                           (contains? arg-alias)))]
              (->> args-spec
                   (filter arg-has-alias)
                   (ffirst))))

          ;; Does the argument still have its default value.?
          (is-default-value-arg? [results arg-key]
            (arg-key (:flag-args results)))

          ;; Is the argument marked as a flag.
          (is-flag-arg? [arg-key]
            (:flag (arg-key args-spec)))

          ;; Get the validator predicate for an argument.
          (get-validate-fn [arg-key]
            (:validate-fn (arg-key args-spec)))
          
          ;; Add a value for an argument.
          (process-arg-value [results arg-key new-value]
            (let [previous-value (arg-key (:results results))]
              (cond (and (not (is-flag-arg? arg-key))
                         (is-default-value-arg? results arg-key)) new-value

                    (and (not (is-flag-arg? arg-key))
                         (not (vector? previous-value))) [previous-value new-value]

                    (not (is-flag-arg? arg-key)) (conj previous-value new-value)

                    :else true)))

          ;; Add an argument to the results map.
          (add-arg [results arg-alias arg-value]
            (let [arg-key (get-arg-key arg-alias)]
              (if (and arg-key
                       (or (not (get-validate-fn arg-key))
                           ((get-validate-fn arg-key) arg-value)))
                (assoc-in results [:results] (assoc (:results results) arg-key (process-arg-value results arg-key arg-value)))
                results)))

          ;; Add a character to the accumulated value so far.
          (accumulate-value [results value & _]
            (assoc-in results [:accumulator] (str (:accumulator results) value)))

          ;; Keep track of the last arguments added to the results.
          (set-last-arg [results arg]
            (assoc-in results [:last-arg] arg))

          ;; Clear out the accumulator.
          (clear-accumulator [results]
            (assoc-in results [:accumulator] ""))

          ;; Mark a flag as one that has had the automatic arg flag added with it.
          (mark-arg-flag [results value]
            (let [arg-key (get-arg-key value)]
              (if arg-key
                (assoc-in results [:flag-args] (assoc (:flag-args results) arg-key true)) 
                results)))
          
          ;; Unmark a flag as one that has had the automatic arg flag added with it.
          (unmark-arg-flag [results value]
            (let [arg-key (get-arg-key value)]
              (if arg-key
                (assoc-in results [:flag-args]
                          (dissoc (:flag-args results) arg-key)) 
                results)))
          
          ;; Add an argument that doesn't have a value. The value is defaulted to true.
          (add-flag-arg [results value & _]
            (-> results
                (mark-arg-flag value)
                (add-arg value true)
                (set-last-arg value)))

          ;; Add a long argument from the accumulator that doesn't have a value.
          ;; The value is defaulted to true.
          (add-long-flag-arg [results value & _]
            (-> results
                (mark-arg-flag (:accumulator results))
                (add-arg (:accumulator results) true)
                (set-last-arg (:accumulator results))
                (clear-accumulator)))

          ;; Add the accumulated value to the last argument added to the results.
          (add-arg-value [results value & _]
            (-> results
                (add-arg (:last-arg results) (:accumulator results))
                (unmark-arg-flag (:last-arg results))
                (clear-accumulator)))

          ;; Convert something to a string.
          (to-str [s]
            (.toString s))]

    ;; The arguments are parsed out using a finite state machine.
    ;; A image of the fsm can be found on this projects github page.
    (let [args-fsm (fsm/defsm args-fsm
                     [[:start
                       "-" -> :arg
                       _ -> :start]
                      
                      [:arg
                       #"[^\s-]" -> {:action add-flag-arg} :arg
                       #"[\s]" -> :value
                       "-" -> :long-arg]
                      
                      [:long-arg
                       #"[^\s-]" -> {:action accumulate-value} :long-arg
                       #"[\s]" -> {:action add-long-flag-arg} :value
                       "-" -> :arg]

                      [:value
                       #"[^\s-]" -> {:action accumulate-value} :value
                       #"[\s]" -> {:action add-arg-value} :value
                       "-" -> :arg]])])
    
    (:results (args-fsm {:accumulator "" :last-arg nil :flag-args {} :results {}}
                        (map to-str (concat (seq args) " "))))))

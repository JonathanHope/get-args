(ns get-args.core
  (require [reduce-fsm :as fsm]))

(defn parse-args
  "Given a spec parse and validate raw command line arguments."
  [args-spec args]
  
  (letfn [;; Get the key for an args alias.
          (get-arg-key [args-spec arg-alias]
            (ffirst (filter (fn [x] (contains? (set (last x)) arg-alias)) args-spec)))

          ;; Is the argument a flag?
          (is-flag-arg? [results arg-key]
            (arg-key (:flag-args results)))

          ;; Add a value for an argument.
          ;; A flag argument is replaced with an actual argument value.
          ;; After that the argument values become a vector of arguments.
          (process-arg-value [results arg-key new-value]
            (let [previous-value (arg-key (:results results))]
              (cond (is-flag-arg? results arg-key) new-value
                    (not (vector? previous-value)) [previous-value new-value]
                    :else (conj previous-value new-value))))

          ;; Add an argument to the results map.
          (add-arg [results arg-alias arg-value]
            (let [arg-key (get-arg-key args-spec arg-alias)]
              (if arg-key
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
            (let [arg-key (get-arg-key args-spec value)]
              (if arg-key
                (assoc-in results [:flag-args] (assoc (:flag-args results) arg-key true)) 
                results)))
          
          ;; Unmark a flag as one that has had the automatic arg flag added with it.
          (unmark-arg-flag [results value]
            (let [arg-key (get-arg-key args-spec value)]
              (if arg-key
                (assoc-in results [:flag-args] (dissoc (:flag-args results) arg-key)) 
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

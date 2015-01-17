(ns get-args.core
  (require [get-args.spec-parser :as sp]
           [get-args.args-parser :as ap]))

(defn parse-args
  "Given a spec parse and validate raw command line arguments."
  [args-spec args]
  (-> args-spec
      (sp/parse-spec)
      (ap/parse-args args)))

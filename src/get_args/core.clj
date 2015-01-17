(ns get-args.core
  (require [get-args.spec-parser :as sp]
           [get-args.args-parser :as ap]
           [get-args.args-validator :as av]))

(defn parse-args
  "Given a spec parse and validate raw command line arguments."
  [spec args]
  (let [parsed-spec (sp/parse-spec spec)]
    (av/validate-args parsed-spec (ap/parse-args parsed-spec args))))

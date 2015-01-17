(ns get-args.core-test
  (:use midje.sweet)
  (:require [get-args.core :refer :all]))

(facts "about `parse-args`"

  (fact "it will return a blank map if there are no args provided."
    (parse-args [] "") => {})

  (fact "it will parse a short arg as flag if not value is provided."
    (parse-args [:verbose ["v"]] "-v") => {:verbose true})
  
  (fact "multiple short flags can be provided."
    (parse-args [:verbose ["v"] :debug ["d"]] "-v -d") => {:verbose true :debug true})

  (fact "multiple short flags and be bunched together."
    (parse-args [:verbose ["v"] :debug ["d"]] "-vd") => {:verbose true :debug true})

  (fact "it parse a long arg as a flag if no value is provided."
    (parse-args [:verbose ["verbose"]] "--verbose") => {:verbose true})

  (fact "multiple long flags can be provided."
    (parse-args [:verbose ["verbose"] :debug ["debug"]] "--verbose --debug") => {:verbose true :debug true})

  (fact "short and long flags can be mixed."
    (parse-args [:verbose ["v"] :debug ["debug"]] "-v --debug") => {:verbose true :debug true}
    (parse-args [:verbose ["v"] :debug ["debug"]] "--debug -v") => {:verbose true :debug true})

  (fact "multiple aliases long and short can be provided for an argument."
    (parse-args [:verbose ["v" "verbose"]] "-v") => {:verbose true}
    (parse-args [:verbose ["v" "verbose"]] "--verbose") => {:verbose true})

  (fact "a value can be provided to short args."
    (parse-args [:file ["f"]] "-f test.txt") => {:file "test.txt"})

  (fact "there can be multipe short arguments with a value."
    (parse-args [:file ["f"] :protocol ["p"]] "-f test.txt -p ftp") => {:file "test.txt" :protocol "ftp"})

  (fact "a value can be provided to a long argument."
    (parse-args [:file ["file"]] "--file test.txt") => {:file "test.txt"})

  (fact "there can be multiple long arguments with a value."
    (parse-args [:file ["file"] :protocol ["protocol"]] "--file test.txt --protocol ftp") => {:file "test.txt" :protocol "ftp"})

  (fact "there can be long and short arguments with a value."
    (parse-args [:file ["f"] :protocol ["protocol"]] "-f test.txt --protocol ftp") => {:file "test.txt" :protocol "ftp"}
    (parse-args [:file ["f"] :protocol ["protocol"]] "--protocol ftp -f test.txt") => {:file "test.txt" :protocol "ftp"})

  (fact "a short argument can have multiple values."
    (parse-args [:file ["f"]] "-f test1.txt test2.txt") => {:file ["test1.txt" "test2.txt"]}
    (parse-args [:file ["f"]] "-f test1.txt test2.txt test3.txt") => {:file ["test1.txt" "test2.txt" "test3.txt"]})

  (fact "a long argument can have multiple values."
    (parse-args [:file ["file"]] "--file test1.txt test2.txt") => {:file ["test1.txt" "test2.txt"]}
    (parse-args [:file ["file"]] "--file test1.txt test2.txt test3.txt") => {:file ["test1.txt" "test2.txt" "test3.txt"]})

  (fact "a short argument can be required."
    (parse-args [:verbose ["v"] {:required true} :debug ["d"]] "-d") => nil
    (parse-args [:verbose ["v"] {:required true} :debug ["d"]] "-v -d") => {:verbose true :debug true})

  (fact "a long argument can be required."
    (parse-args [:verbose ["verbose"] {:required true} :debug ["debug"]] "--debug") => nil
    (parse-args [:verbose ["verbose"] {:required true} :debug ["debug"]] "--debug --verbose") => {:verbose true :debug true}))

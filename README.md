# get-args

A library to specify and gather command line arguments provided in the GNU format.

## Installation

Include the following dependency in your project.clj file:

        :dependencies [[get-args "0.1.0"]]

## Usage

First define a spec:

        [:verbose ["v" "verbose"] :inputs ["i" "inputs"] :output ["o" "output"]]

The spec is passed to the parser with the raw arguments and a map of the provided arguments is returned. For example:

        "-v --inputs input1.txt input2.txt -o output.txt"
        ;; => {:output "output.txt", :inputs ["input1.txt" "input2.txt"], :verbose true}

A spec is a vector with a repeated sequence of a keyword, a vector of aliases, and a optional options map. Any alias that is a single character is invoked like "-v" and any alias that is more than one character is invoked like "--verbose". By default an argument will first be added with a value of true. Once the a value is found for the argument the default value of true is replaced with the actual value. If there is another value for the arugment the value will become an array of values. That behavior can be configured with options.

## Options

### flag

An argument marked as a flag will not accept any arguments.

        [:verbose ["v" "verbose"] {:flag true}]
        "-v test.txt"
        ;; => {:verbose true}

### required

An argument marked as required must be provided or no arguments are returned.

        [:verbose ["v" "verbose"] {:required true} :verbose ["f" "file"]]
        "-f test.txt"
        ;; => nil

### parse-fn

An argument with a parse function provided will have the parser function applied to the raw value.

       [:level ["l"] {:parse-fn (fn [x] (Integer. x))}]
       "-l 5"
       ;; => {:level 5}

### validate-fn

An argument with a validate predicate will not acummulate any values that don't pass the predicate. 

        [:file ["f"] {:validate-fn (fn [x] false)} :verbose ["v"]]
        "-f test.txt -v"
        ;; => {:verbose true}

## Usage

        (require [get-args.core :as ga])

        (defn -main
          [& args]
          (ga/parse-args [:verbose ["v" "verbose"]] args))

## License

Copyright © 2015 Jonathan Hope

Distributed under the MIT license.

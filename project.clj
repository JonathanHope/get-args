(defproject get-args "0.1.1"
  :description "Parses and validates command line arguments."
  :url "https://github.com/JonathanHope/get-args"

  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [reduce-fsm "0.1.3"]
                 [midje "1.6.3"]]

  :plugins [[lein-midje "3.1.3"]]

  :scm {:name "git"
        :url "https://github.com/JonathanHope/get-args"})

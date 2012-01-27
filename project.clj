(defproject w-import-gen "1.0.0-SNAPSHOT"
  :description "some manipulation data only useful to us."
  :dependencies [[org.clojure/clojure     "1.3.0"]
                 [org.clojure/tools.cli   "0.2.1"]
                 [clojure-csv/clojure-csv "1.3.2"]]
  :dev-dependencies [[midje "1.3.1"]
                     [com.intelie/lazytest "1.0.0-SNAPSHOT" :exclusions [swank-clojure]]]
  :main w-import-gen.io.main/-main)

(ns ^{:doc "Analyse a content file fo find the distribution of the MALs per content"}
  w-import-gen.io.analyse
  (:use [w-import-gen.core :only [attributes models contents make-meta]]
        [midje.sweet]
        [clojure.tools.cli]
        [clojure.repl
         :only [doc]]))

(println "--------- BEGIN OF ANALYSE  ----------" (java.util.Date.))

(def nb-items-model-before-mals 7)

(defn model-count-line "Take a line a return the number of MALs"
  [nb-items-before l] (- (count (remove empty? l)) nb-items-before) )

(fact "model-count-line"
      (model-count-line 1 ["stuff" "mal1" "mal2"]) => 2
      (model-count-line 1 ["stuff"  "mal1" "mal2" ""]) => 2)

(defn model-count-mal
  [f] (println "Counting mal from " f))

(defn -main [& args]
  (let [[options args banner :as opts]
        (cli args
             ["-h" "--help"           "Show help" :default false :flag true]
             ["-f" "--model-filename" "CSV file to read"])]

    (when (options :help)
      (println banner)
      (System/exit 0))
    ;; generates the import files
    (model-count-mal (options :model-filename))))


(println "--------- END OF ANALYSE  ----------" (java.util.Date.))

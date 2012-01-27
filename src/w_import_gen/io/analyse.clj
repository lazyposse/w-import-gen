(ns ^{:doc "Analyse a content file fo find the distribution of the MALs per content"}
  w-import-gen.io.analyse
  (:use [w-import-gen.core    :only [attributes models contents make-meta]]
        [w-import-gen.io.util :only [read-lines]]
        [midje.sweet]
        [clojure.tools.cli]
        [clojure.repl :only [doc]]
        [clojure-csv.core :only [parse-csv]]))

(unfinished )

(def nb-items-model-before-mals 7)

(defn model-count-line "Take a line a return the number of MALs"
  [nb-items-before l] (- (count (remove empty? l)) nb-items-before) )

(fact "model-count-line"
      (model-count-line 1 ["stuff" "mal1" "mal2"]) => 2
      (model-count-line 1 ["stuff"  "mal1" "mal2" ""]) => 2)

(defn model-count-mals
  [lines] (reduce (fn [r l]
                    (+ r (model-count-line nb-items-model-before-mals
                                           (parse-csv l))))
                  0
                  (rest lines)))

(fact "model-count-mals"
      (model-count-mals [:h :l1 :l2]) => 3
      (provided
       (parse-csv :l1) => :cols1
       (parse-csv :l2) => :cols2
       (model-count-line nb-items-model-before-mals :cols1) => 1
       (model-count-line nb-items-model-before-mals :cols2) => 2))

(defn model-count-mal
  [f] (model-count-mals (read-lines f)))

(fact "model-count-mal"
      (model-count-mal :filename) => :res
      (provided
       (read-lines :filename)    => :lines
       (model-count-mals :lines) => :res))

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



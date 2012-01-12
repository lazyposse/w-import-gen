(ns ^{:doc "Generate import files - io namespace"}
  w_import_gen.import_gen_io
  (:use [w_import_gen.import_gen]
        [midje.sweet]
        [clojure.tools.cli])
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io])
  (:gen-class))

(println "--------- BEGIN OF IO  ----------" (java.util.Date.))

(defn lazy-write-lines "Take a seq and a filename and lazily write the seq to the file, each element being on a separate line"
  [f s]
  (with-open [w (io/writer f)]
    (binding [*out* w]
      (doseq [l s]
        (println l)))))

(fact "lazy-write-lines"
  (let [filename "/tmp/lazy-write-lines.txt"]
    (lazy-write-lines filename [1 2])   => nil
    (:out (shell/sh "cat" filename)) => "1\n2\n"))

(defn attr-file "Output a file of attributes import file"
  [attr-codes]
  (lazy-write-lines "/tmp/attr.csv"
                    (cons (attr-head)
                          (map attr-line attr-codes))))

(fact "attr-file"
  (attr-file ["a0" "a1"]) => nil
  (:out (shell/sh "cat" "/tmp/attr.csv")) => "\"ACTION\";\"TYPE D'OBJET\";\"CODE CARACTERISTIQUE\";\"SOURCE\";\"NOM\";\"NOM D'AFFICHAGE\";\"UNITE DE MESURE\";\"TYPE D'ATTRIBUT\"\n\"CREATE\";\"Attribute\";\"a0\";\"SID\";\"name-a0\";\"display-name-a0\";;\"string\"\n\"CREATE\";\"Attribute\";\"a1\";\"SID\";\"name-a1\";\"display-name-a1\";;\"string\"\n")

(defn model-file "Given a seq of model codes and a seq of model attributes, Write a model import file"
  [models attrs nb-attrs-per-model]
  (lazy-write-lines "/tmp/model.csv"
                    (cons (model-head)
                          (map (fn [model-code]
                                 (model-line model-code
                                             nb-attrs-per-model
                                             attrs ))
                               models))))

(fact "model-file"
  (model-file ["m0" "m1"]  ["a0" "a1" "a2"] 2) => nil
  (:out (shell/sh "cat" "/tmp/model.csv")) => "\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\n\"CREATE\";\"Model\";\"m0\";\"SID\";\"Product\";\"name-m0\";\"display-name-m0\";\"a0|true|false|\";\"a1|true|false|\";\n\"CREATE\";\"Model\";\"m1\";\"SID\";\"Product\";\"name-m1\";\"display-name-m1\";\"a0|true|false|\";\"a1|true|false|\";\n")

(defn content-file "Given a seq of model codes, a seq of attribute codes and a number of contents to produce, write a contents import file"
  [models attrs nb-attrs-per-model content-nb ]
  (lazy-write-lines "/tmp/content.csv"
                    (cons (content-head)
                          (take content-nb
                                (cycle
                                 (map (fn [model-code]
                                        (content-line model-code nb-attrs-per-model attrs))
                                      models))))))

;.;. The next function taunts you still. Will you rise to the challenge? --
;.;. anonymous
(fact "content-file"
  (content-file ["m0" "m1"]  ["a0" "a1" "a2"] 1 2) => nil
  (:out (shell/sh "cat" "/tmp/content.csv")) => "\"ACTION\";\"TYPE D'OBJET\";\"ID CONTENU\";\"SOURCE\";\"CODE DU MODELE\";\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\n\"CREATE\";\"Content\";;\"SITELABO\";\"m0\";\"a0\";\"a0-val\"\n\"CREATE\";\"Content\";;\"SITELABO\";\"m1\";\"a0\";\"a0-val\"\n")

(defn all-file
  "Given a seq of attribute codes and a seq of model codes, 3 import files are generated:
   - one for attributes
   - one for models
   - one for contents
   Each model have all the attributes as MALs.
   Each content has all the attributes of the model and attribute values generated."
  [args]
  (let [{:keys [attributes models]} (make-meta args)]
    (attr-file    attributes)
    (model-file   models attributes (:nb-attrs-per-model args))
    (content-file models attributes (:nb-attrs-per-model args) (:nb-contents args))))

(defn -main [& args]
  (let [[options args banner :as opts]
        (cli args
             ["-a" "--nb-attributes"      "Number of attributes to generate"           :parse-fn #(Integer. %) :default 10]
             ["-a" "--nb-attrs-per-model" "Number of attributes per model to generate" :parse-fn #(Integer. %) :default 3]
             ["-m" "--nb-models"          "Number of models to generate"               :parse-fn #(Integer. %) :default 100]
             ["-c" "--nb-contents"        "Number of contents to generate"             :parse-fn #(Integer. %) :default 200])]

    (when (options :help)
      (println banner)
      (System/exit 0))

    (println "Generating" (options :nb-attributes) "attributes," (options :nb-models) "models -" (options :nb-attrs-per-model) "attributes per model - and" (options :nb-contents) "contents...")
    
    ;; generates the import files
    (all-file options)
    (println "done!")))

(future-fact "IT test - A small data set"
             (let [n (all-file {:nb-attributes       3
                                :nb-models           2
                                :attrs-per-model-nb  1
                                :nb-contents         10})]
               (:out (shell/sh "cat" "/tmp/attr.csv")) => nil
               (:out (shell/sh "cat" "/tmp/model.csv")) => nil
               (:out (shell/sh "cat" "/tmp/content.csv")) => nil))

(comment "A 'SID-like' data set"
         (all-file {:nb-attributes       7500
                    :nb-models           2000
                    :attrs-per-model-nb  450
                    :nb-contents         130000}))

(println "--------- END OF IO  ----------" (java.util.Date.))

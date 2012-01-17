(ns ^{:doc "Generate import files - io namespace"}
  w-import-gen.io.util
  (:use [w-import-gen.core :only [attributes models contents make-meta]]
        [midje.sweet]
        [clojure.tools.cli])
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io]))

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

(comment (defn lazy-read-lines "lazy read the file"
   ([f s] (with-open [r (io/reader f)]
            (binding [*in* r]
              (doseq [l s]
                ( l)))))))

(defn lazy-read-lines "lazy read the file"
  ([f] (with-open [r (io/reader f)]
         (binding [*in* r]
           (lazy-read-lines r (read-line)))))
  ([r l]  (cons l
                (lazy-seq (lazy-read-lines r (read-line))))))

(future-fact "lazy-write-lines"
  (let [filename "/tmp/lazy-write-lines.txt"]
    (lazy-write-lines filename [1 2])   => nil
    (lazy-read-lines filename) => "1\n2\n"))

(defn attr-file "Output a file of attributes import file"
  [attr-codes]
  (lazy-write-lines "/tmp/attr.csv" (attributes attr-codes)))

(fact "attr-file"
  (attr-file ["a0" "a1"]) => nil
  (provided
    (attributes ["a0" "a1"]) => ["header", "attr;a0", "attr;a1"])
  (:out (shell/sh "cat" "/tmp/attr.csv")) => "header\nattr;a0\nattr;a1\n")

(defn model-file "Given a seq of model codes and a seq of model attributes, Write a model import file"
  [model-codes attr-codes nb-attrs-per-model]
  (lazy-write-lines "/tmp/model.csv" (models model-codes attr-codes nb-attrs-per-model)))

(fact "model-file"
  (model-file ["m0" "m1"]  ["a0" "a1" "a2"] 2) => nil
  (provided
    (models ["m0" "m1"]  ["a0" "a1" "a2"] 2) => ["header", "m0;a0;a1", "m1;a0;a1"])
  (:out (shell/sh "cat" "/tmp/model.csv")) => "header\nm0;a0;a1\nm1;a0;a1\n")

(defn content-file "Given a seq of model codes, a seq of attribute codes and a number of contents to produce, write a contents import file"
  [model-codes attr-codes nb-attrs-per-model content-nb]
  (lazy-write-lines "/tmp/content.csv" (contents model-codes attr-codes nb-attrs-per-model content-nb )))

(fact "content-file"
  (content-file ["m0" "m1"]  ["a0" "a1" "a2"] 1 2) => nil
    (provided
    (contents ["m0" "m1"]  ["a0" "a1" "a2"] 1 2) => ["header", "c0;m0;a0;a1", "c1;m1;a0;a1"])
  (:out (shell/sh "cat" "/tmp/content.csv")) => "header\nc0;m0;a0;a1\nc1;m1;a0;a1\n")

(defn all-file
  "Given a seq of attribute codes and a seq of model codes, 3 import files are generated:
   - one for attributes
   - one for models
   - one for contents
   Each model have all the attributes as MALs.
   Each content has all the attributes of the model and attribute values generated."
  [args]
  (let [{:keys [attr-codes model-codes]} (make-meta args)]
    (attr-file    attr-codes)
    (model-file   model-codes attr-codes (:nb-attrs-per-model args))
    (content-file model-codes attr-codes (:nb-attrs-per-model args) (:nb-contents args))))

(defn -main [& args]
  (let [[options args banner :as opts]
        (cli args
             ["-h" "--help"               "Show help"                                  :default false :flag true]
             ["-a" "--nb-attributes"      "Number of attributes to generate"           :parse-fn #(Integer. %) :default 10]
             ["-p" "--nb-attrs-per-model" "Number of attributes per model to generate" :parse-fn #(Integer. %) :default 3]
             ["-m" "--nb-models"          "Number of models to generate"               :parse-fn #(Integer. %) :default 100]
             ["-c" "--nb-contents"        "Number of contents to generate"             :parse-fn #(Integer. %) :default 200])]

    (when (options :help)
      (println banner)
      (System/exit 0))

    (println "Generating" (options :nb-attributes) "attributes," (options :nb-models) "models -" (options :nb-attrs-per-model) "attributes per model - and" (options :nb-contents) "contents...")
    
    ;; generates the import files
    (all-file options)
    (println "done!")))

(fact "IT test - A small data set"
  (let [n (all-file {:nb-attributes      1
                    :nb-models           1
                    :nb-attrs-per-model  1
                    :nb-contents         1})]
    (:out (shell/sh "cat" "/tmp/attr.csv")) => "\"ACTION\";\"TYPE D'OBJET\";\"CODE CARACTERISTIQUE\";\"SOURCE\";\"NOM\";\"NOM D'AFFICHAGE\";\"UNITE DE MESURE\";\"TYPE D'ATTRIBUT\"\n\"CREATE\";\"Attribute\";\"a1\";\"SID\";\"name-a1\";\"display-name-a1\";;\"string\"\n"
    (:out (shell/sh "cat" "/tmp/model.csv")) => "\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\n\"CREATE\";\"Model\";\"m1\";\"SID\";\"Product\";\"name-m1\";\"display-name-m1\";\"a1|true|false|\";\n"
    (:out (shell/sh "cat" "/tmp/content.csv")) => "\"ACTION\";\"TYPE D'OBJET\";\"ID CONTENU\";\"SOURCE\";\"CODE DU MODELE\";\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\n\"CREATE\";\"Content\";;\"SITELABO\";\"m1\";\"a1\";\"a1-val\"\n"))

(println "--------- END OF IO  ----------" (java.util.Date.))

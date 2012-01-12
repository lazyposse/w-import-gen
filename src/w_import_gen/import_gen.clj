(ns ^{:doc "genereate import files"}
  clojure-station.wikeo.import-gen
  (:use     [midje.sweet])
  (:use     [clojure.tools.cli])
  (:use     [clojure.pprint :only [pp pprint]])
  (:require [clojure.walk :as w])
  (:require [clojure.set  :as set])
  (:require [clojure.zip  :as z])
  (:require [clojure.java.shell :as shell])
  (:require [clojure.java.io :as io])
  (:import (java.util Date))
  (:gen-class))

(println "--------- BEGIN OF IMPORT_GEN  ----------" (java.util.Date.))


(defn lazy-write-lines "Take a seq and a filename and lazily write the seq to the file, each element being on a separate line"
  [f s] (with-open [w (io/writer f)]
          (binding [*out* w]
            (doseq [l s]
              (println l)))))

(fact "lazy-write-lines"
      (let [filename "/tmp/lazy-write-lines.txt"]
        (lazy-write-lines filename [1 2])   => nil
        (:out (shell/sh "cat" filename)) => "1\n2\n"))

(defn- attr-head "The header of an attribute import file"
  []
  "\"ACTION\";\"TYPE D'OBJET\";\"CODE CARACTERISTIQUE\";\"SOURCE\";\"NOM\";\"NOM D'AFFICHAGE\";\"UNITE DE MESURE\";\"TYPE D'ATTRIBUT\"")

(defn- attr-line "Returns a line for an attribute import file"
  [code]
  (str "\"CREATE\";\"Attribute\";\"" code "\";\"SID\";\"name-" code "\";\"display-name-" code "\";;\"string\""))

(fact "attr-line"
      (attr-line "titre") => "\"CREATE\";\"Attribute\";\"titre\";\"SID\";\"name-titre\";\"display-name-titre\";;\"string\"")

(defn attr-file "Output a file of attribute import file"
  [attr-codes] (lazy-write-lines "/tmp/attr.csv"
                      (cons (attr-head)
                            (map attr-line attr-codes))))

(defn- model-head "Return a header of a import model file"
  [] "\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";")

(defn- model-line "Return a line of a model import line"
  [model-code mal-nb attribut-codes]
  (str "\"CREATE\";\"Model\";\"" model-code "\";\"SID\";\"Product\";\"name-" model-code "\";\"display-name-" model-code "\";"
       (reduce str (mapcat (fn [attr-code] (str "\"" attr-code "|true|false|\";"))
                           (take mal-nb attribut-codes)))))

(fact "model-line"
      (model-line "ProduitRosier" 0 ["reference" "designation" "foo"]) =>
      "\"CREATE\";\"Model\";\"ProduitRosier\";\"SID\";\"Product\";\"name-ProduitRosier\";\"display-name-ProduitRosier\";")

(fact "model-line"
      (model-line "ProduitRosier" 2 ["reference" "designation" "foo"]) =>
      "\"CREATE\";\"Model\";\"ProduitRosier\";\"SID\";\"Product\";\"name-ProduitRosier\";\"display-name-ProduitRosier\";\"reference|true|false|\";\"designation|true|false|\";")

(defn model-file "Given a seq of model codes and a seq of model attributes, Write a model import file"
  [models attrs] (lazy-write-lines
                  "/tmp/model.csv"
                  (cons (model-head)
                        (map (fn [model-code]
                               (model-line model-code
                                           (count attrs)
                                           attrs ))
                             models))))

(defn- content-head "Return the header of a content import file"
  [] (str "\"ACTION\";\"TYPE D'OBJET\";\"ID CONTENU\";\"SOURCE\";\"CODE DU MODELE\";"
          (reduce str (repeat 53 "\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\""))))

(defn- content-line "Given a model code, a number of attribute and a seq of all attributes codes, return a line of content import file"
  [model-code attr-nb attr-codes]
  (str "\"CREATE\";\"Content\";;\"SITELABO\";\"" model-code "\""
       (reduce str (mapcat #(str ";\"" % "\";\"" % "-val\"" )
                           (take attr-nb attr-codes)))))

(fact "content-line"
      (content-line "ServiceRosier"
                    2
                    ["attr1" "attr2"])
      =>
      "\"CREATE\";\"Content\";;\"SITELABO\";\"ServiceRosier\";\"attr1\";\"attr1-val\";\"attr2\";\"attr2-val\"")

(defn content-file "Given a seq of model codes, a seq of attributes code an a number of content to produce, write a content import file"
  [models attrs content-nb]
  (lazy-write-lines "/tmp/content.csv"
                    (cons (content-head)
                          (take content-nb
                                (cycle
                                 (map (fn [model-code]
                                        (content-line model-code (count attrs) attrs))
                                      models))))))

(defn make-meta "Given a number of models and an number of attributes per model, return a seq of model codes and a seq of attribute codes"
  [{:keys [model-nb attrs-per-model-nb]}]
  {:attributes (map #(str "a" (inc %)) (range attrs-per-model-nb))
   :models     (map #(str "m" (inc %)) (range model-nb))})

(fact "make-meta"
  (make-meta {:model-nb           2
              :attrs-per-model-nb 3}) => {:attributes ["a1" "a2" "a3"]
                                          :models     ["m1" "m2"]})

(defn all-file "Given a seq of attribute codes and a seq of model code, 3 files import files: one for attribute, one for models and one for content, each models has all the attributes and each content has all the attributes."
  [args]
  (let [{:keys [attributes models]}
        (make-meta args)]
    (attr-file    attributes)
    (model-file   models attributes)
    (content-file models attributes (:content-nb args))))

(defn -main [& args]
  (let [[options args banner :as opts]
        (cli args
             ["-a" "--attributes" "Number of attributes per model to generate" :parse-fn #(Integer. %) :default 80] 
             ["-m" "--models"     "Number of models to generate"               :parse-fn #(Integer. %) :default 10]
             ["-c" "--contents"   "Number of contents to generate"             :parse-fn #(Integer. %) :default 100])]
    ;; deal with 
    (when (options :help)
      (println banner)
      (System/exit 0))
    (println "Generating" (options :attributes) "attributes", (options :models) "models and" (options :contents) "contents...")
    ;; generates the import files
    ;; TODO could be improved to directly match the arg map instead of transcoding
    (all-file {:model-nb           (options :models)
               :attrs-per-model-nb (options :attributes)
               :content-nb         (options :contents)}   )
    (println "done!")))    

(comment "A small data set"
  (all-file {:model-nb           2
             :attrs-per-model-nb 3
             :content-nb         10}))

(comment "A 'SID-like' data set"
  (all-file {:model-nb           2000
             :attrs-per-model-nb 450
             :content-nb         130000}))

(println "--------- END OF IMPORT_GEN  ----------" (java.util.Date.))

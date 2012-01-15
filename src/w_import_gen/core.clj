(ns ^{:doc "Generate list of lines for the csv import files - namespace"}
  w-import-gen.core
  (:use [midje.sweet]))

(println "--------- BEGIN OF IMPORT_GEN  ----------" (java.util.Date.))

(defn- attr-head
  "The header of an attributes import file"
  []
  "\"ACTION\";\"TYPE D'OBJET\";\"CODE CARACTERISTIQUE\";\"SOURCE\";\"NOM\";\"NOM D'AFFICHAGE\";\"UNITE DE MESURE\";\"TYPE D'ATTRIBUT\"")

(defn- attr-line
  "Returns a line for an attributes import file"
  [code]
  (str "\"CREATE\";\"Attribute\";\"" code "\";\"SID\";\"name-" code "\";\"display-name-" code "\";;\"string\""))

(fact "attr-line"
      (attr-line "titre") => "\"CREATE\";\"Attribute\";\"titre\";\"SID\";\"name-titre\";\"display-name-titre\";;\"string\"")

(defn attributes
  "Generates a seq of lines for the attributes import file."
  [attr-codes]
  (cons (attr-head)
        (map attr-line attr-codes)))

(fact "attr"
  (attr ["a0" "a1"]) => ["\"ACTION\";\"TYPE D'OBJET\";\"CODE CARACTERISTIQUE\";\"SOURCE\";\"NOM\";\"NOM D'AFFICHAGE\";\"UNITE DE MESURE\";\"TYPE D'ATTRIBUT\""
                         "\"CREATE\";\"Attribute\";\"a0\";\"SID\";\"name-a0\";\"display-name-a0\";;\"string\"" "\"CREATE\";\"Attribute\";\"a1\";\"SID\";\"name-a1\";\"display-name-a1\";;\"string\""])

(defn- model-head
  "Return a header of an imports model file"
  [] "\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";")

(defn- model-line
  "Return a line of a models import file"
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

(defn models
  "Given a seq of model codes and a seq of model attributes, create a seq of model import lines"
  [models attrs nb-attrs-per-model]
  (cons (model-head)
        (map (fn [model-code]
               (model-line model-code
                           nb-attrs-per-model
                           attrs ))
             models)))

(fact "models"
  (models ["m0" "m1"]  ["a0" "a1" "a2"] 2) => ["\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";"
                                               "\"CREATE\";\"Model\";\"m0\";\"SID\";\"Product\";\"name-m0\";\"display-name-m0\";\"a0|true|false|\";\"a1|true|false|\";" "\"CREATE\";\"Model\";\"m1\";\"SID\";\"Product\";\"name-m1\";\"display-name-m1\";\"a0|true|false|\";\"a1|true|false|\";"])

(defn- content-head
  "Return the header of a contents import file"
  [] (str "\"ACTION\";\"TYPE D'OBJET\";\"ID CONTENU\";\"SOURCE\";\"CODE DU MODELE\";"
          (reduce str (repeat 53 "\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\""))))

(defn- content-line
  "Given a model code, a number of attributes and a seq of all attributes codes, return a line of contents import file"
  [model-code attr-nb attr-codes]
  (str "\"CREATE\";\"Content\";;\"SITELABO\";\"" model-code "\""
       (reduce str (mapcat #(str ";\"" % "\";\"" % "-val\"" )
                           (take attr-nb attr-codes)))))

(fact "content-line"
      (content-line "ServiceRosier" 2 ["attr1" "attr2"]) =>
      "\"CREATE\";\"Content\";;\"SITELABO\";\"ServiceRosier\";\"attr1\";\"attr1-val\";\"attr2\";\"attr2-val\"")

(defn contents
  "Given a seq of model codes, a seq of attribute codes and a number of contents to produce, generate a seq of contents line"
  [models attrs nb-attrs-per-model content-nb]
  (cons (content-head)
        (take content-nb
              (cycle
               (map (fn [model-code]
                      (content-line model-code nb-attrs-per-model attrs))
                    models)))))

(fact "contents"
  (contents ["m0" "m1"]  ["a0" "a1" "a2"] 1 2) => ["\"ACTION\";\"TYPE D'OBJET\";\"ID CONTENU\";\"SOURCE\";\"CODE DU MODELE\";\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\"\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\""
                                                   "\"CREATE\";\"Content\";;\"SITELABO\";\"m0\";\"a0\";\"a0-val\"" "\"CREATE\";\"Content\";;\"SITELABO\";\"m1\";\"a0\";\"a0-val\""])

(defn make-meta
  "Given a number of models and a number of attributes per model, return a seq of model codes and a seq of attribute codes"
  [{:keys [nb-attributes nb-models]}]
  {:attributes (map #(str "a" (inc %)) (range nb-attributes))
   :models     (map #(str "m" (inc %)) (range nb-models))})

(fact "make-meta"
  (make-meta {:nb-models     2
              :nb-attributes 3}) => {:attributes ["a1" "a2" "a3"]
                                     :models     ["m1" "m2"]})

(println "--------- END OF IMPORT_GEN  ----------" (java.util.Date.))

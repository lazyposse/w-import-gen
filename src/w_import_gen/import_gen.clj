(ns ^{:doc "genereate import files"}
  clojure-station.wikeo.import-gen
  (:use     [midje.sweet]
            [clojure.pprint :only [pp pprint]]
            [clojure.tools.cli])
  (:require [clojure.walk :as w]
            [clojure.set  :as set]
            [clojure.zip  :as z]
            [clojure.java.shell :as shell]
            [clojure.string :as s :only [join]])
  (:gen-class))

(println "--------- BEGIN OF IMPORT_GEN  ----------" (java.util.Date.))

(defn as-lines [s] (str (s/join "\n" s) "\n"))

(fact (as-lines [1 2]) => "1\n2\n")

(defn spit-as-lines "Given a seq and a filename, write it to the specified file as lines."
  [f s] (spit f (as-lines s)))

(fact "spit-as-lines"
      (let [filename "/tmp/spit-as-lines.txt"]
        (spit-as-lines filename [1 2])   => nil
        (:out (shell/sh "cat" filename)) => "1\n2\n"))

(defn- attr-head
  []
  "ACTION;TYPE D'OBJET;CODE CARACTERISTIQUE;SOURCE;NOM;NOM D'AFFICHAGE;UNITE DE MESURE;TYPE D'ATTRIBUT")

(defn- attr-line
  [code]
  (str "CREATE;Attribute;" code ";SID;name-" code ";display-name-" code ";;string"))

(fact "attr-line"
      (attr-line "titre") => "CREATE;Attribute;titre;SID;name-titre;display-name-titre;;string")

(defn attr-file
  [attr-codes] (spit-as-lines "/tmp/gen-attr.csv"
                      (cons (attr-head)
                            (map attr-line attr-codes))))

(defn- model-head
  [] "ACTION;TYPE D'OBJET;CODE DU MODELE;SOURCE;TYPE DE MODELE;NOM;NOM D'AFFICHAGE;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;MAL;")

(defn- model-line
  [model-code nb attribut-codes]
  (str "CREATE;Model;" model-code ";SID;Product;name-" model-code ";display-name-" model-code ";"
       (reduce str (mapcat (fn [attr-code] (str "" attr-code "|true|false|;"))
                           (take nb attribut-codes)))))

(fact "model-line"
      (model-line "ProduitRosier" 0 ["reference" "designation" "foo"]) =>
      "CREATE;Model;ProduitRosier;SID;Product;name-ProduitRosier;display-name-ProduitRosier;")

(fact "model-line"
      (model-line "ProduitRosier" 2 ["reference" "designation" "foo"]) =>
      "CREATE;Model;ProduitRosier;SID;Product;name-ProduitRosier;display-name-ProduitRosier;reference|true|false|;designation|true|false|;")

(defn model-file
  [model->attrs] (spit-as-lines
                  "/tmp/gen-model.csv"
                  (cons (model-head)
                        (map (fn [model-code]
                               (let [attrs (model->attrs model-code)]
                                 (model-line model-code
                                             (count attrs)
                                             attrs )))
                             (keys model->attrs)))))

(defn- content-head
  [] (str "ACTION;TYPE D'OBJET;ID CONTENU;SOURCE;CODE DU MODELE;"
          (reduce str (repeat 53 "CODE DE L'ATTRIBUT;VALEUR DE L'ATTRIBUT;"))))

(defn- content-line
  [model-code attr-nb attr-codes]
  (str "CREATE;Content;;SITELABO;" model-code ";"
       (reduce str (mapcat #(str "" % ";" % "-val;" )
                           (take attr-nb attr-codes)))))

(fact "content-line"
      (content-line "ServiceRosier"
                    2
                    ["attr1" "attr2"])
      =>
      "CREATE;Content;;SITELABO;ServiceRosier;attr1;attr1-val;attr2;attr2-val;")

(defn content-file
  [model->attrs content-nb]
  (spit-as-lines "/tmp/gen-content.csv"
                 (cons (content-head)
                       (take content-nb
                             (cycle
                              (map (fn [model-code]
                                     (let [attrs (model->attrs model-code)]
                                       (content-line model-code (count attrs) attrs)))
                                   (keys model->attrs)))))))

(defn new-model->attrs
  [attr-nb model-nb]
  (zipmap (map #(str "m" (inc %)) (range model-nb))
          (map (fn [_] (map #(str "a" (inc %)) (range attr-nb)))
               (range model-nb))))


(fact "new-model->attrs"
      (new-model->attrs 3 2) => {"m1" ["a1" "a2" "a3"]
                                 "m2" ["a1" "a2" "a3"]})

(defn all-file
  [attr-nb model-nb content-nb]
  (let [model->attrs (new-model->attrs attr-nb model-nb)
        attrs        (distinct (mapcat second model->attrs))]
    (attr-file attrs)
    (model-file model->attrs)
    (content-file model->attrs content-nb)))

(defn -main [& args]
  (let [[options args banner :as opts]
        (cli args
             ["-a" "--attributes" "Number of attributes to generate" :parse-fn #(Integer. %) :default 80] 
             ["-m" "--models"     "Number of models to generate"     :parse-fn #(Integer. %) :default 10]
             ["-c" "--contents"   "Number of contents to generate"   :parse-fn #(Integer. %) :default 100])]
    
    ;; deal with 
    (when (options :help)
      (println banner)
      (System/exit 0))

    (println "Generating" (options :attributes) "attributes", (options :models) "models and" (options :contents) "contents...")
    
    ;; generates the import files
    (all-file (options :attributes) (options :models) (options :contents))
    (println "done!")))    

(println "--------- END OF IMPORT_GEN  ----------" (java.util.Date.))

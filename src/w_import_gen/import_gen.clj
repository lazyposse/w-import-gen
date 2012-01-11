(ns ^{:doc "genereate import files"}
  clojure-station.wikeo.import-gen
  (:use     [midje.sweet])
  (:use     [clojure.pprint :only [pp pprint]])
  (:require [clojure.walk :as w])
  (:require [clojure.set  :as set])
  (:require [clojure.zip  :as z])
  (:require [clojure.java.shell :as shell])
  (:require [clojure.java.io :as io])
  (:import (java.util Date))
  (:gen-class))

(println "--------- BEGIN OF IMPORT_GEN  ----------" (java.util.Date.))

(fact (as-lines [1 2]) => "1\n2\n")

(defn lazy-write-lines "Take a seq and a filename and lazily write the seq to the file, each element being on a separate line"
  [f s] (with-open [w (io/writer f)]
          (binding [*out* w]
            (doseq [l s]
              (println l)))))

(fact "lazy-write-lines"
      (let [filename "/tmp/lazy-write-lines.txt"]
        (lazy-write-lines filename [1 2])   => nil
        (:out (shell/sh "cat" filename)) => "1\n2\n"))

(defn- attr-head
  []
  "\"ACTION\";\"TYPE D'OBJET\";\"CODE CARACTERISTIQUE\";\"SOURCE\";\"NOM\";\"NOM D'AFFICHAGE\";\"UNITE DE MESURE\";\"TYPE D'ATTRIBUT\"")

(defn- attr-line
  [code]
  (str "\"CREATE\";\"Attribute\";\"" code "\";\"SID\";\"name-" code "\";\"display-name-" code "\";;\"string\""))

(fact "attr-line"
      (attr-line "titre") => "\"CREATE\";\"Attribute\";\"titre\";\"SID\";\"name-titre\";\"display-name-titre\";;\"string\"")

(defn attr-file
  [attr-codes] (lazy-write-lines "/tmp/attr.csv"
                      (cons (attr-head)
                            (map attr-line attr-codes))))

(defn- model-head
  [] "\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";")

(defn- model-line
  [model-code nb attribut-codes]
  (str "\"CREATE\";\"Model\";\"" model-code "\";\"SID\";\"Product\";\"name-" model-code "\";\"display-name-" model-code "\";"
       (reduce str (mapcat (fn [attr-code] (str "\"" attr-code "|true|false|\";"))
                           (take nb attribut-codes)))))

(fact "model-line"
      (model-line "ProduitRosier" 0 ["reference" "designation" "foo"]) =>
      "\"CREATE\";\"Model\";\"ProduitRosier\";\"SID\";\"Product\";\"name-ProduitRosier\";\"display-name-ProduitRosier\";")

(fact "model-line"
      (model-line "ProduitRosier" 2 ["reference" "designation" "foo"]) =>
      "\"CREATE\";\"Model\";\"ProduitRosier\";\"SID\";\"Product\";\"name-ProduitRosier\";\"display-name-ProduitRosier\";\"reference|true|false|\";\"designation|true|false|\";")

(defn model-file
  [models attrs] (lazy-write-lines
                  "/tmp/model.csv"
                  (cons (model-head)
                        (map (fn [model-code]
                               (model-line model-code
                                           (count attrs)
                                           attrs ))
                             models))))

(defn- content-head
  [] (str "\"ACTION\";\"TYPE D'OBJET\";\"ID CONTENU\";\"SOURCE\";\"CODE DU MODELE\";"
          (reduce str (repeat 53 "\"CODE DE L'ATTRIBUT\";\"VALEUR DE L'ATTRIBUT\""))))

(defn- content-line
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

(defn content-file
  [models attrs content-nb]
  (lazy-write-lines "/tmp/content.csv"
                    (cons (content-head)
                          (take content-nb
                                (cycle
                                 (map (fn [model-code]
                                        (content-line model-code (count attrs) attrs))
                                      models))))))

(defn make-meta "Create a map of wikeo meta data"
  [{:keys [model-nb attrs-per-model-nb]}]
  {:attributes (map #(str "a" (inc %)) (range attrs-per-model-nb))
   :models     (map #(str "m" (inc %)) (range model-nb))})

(fact "make-meta"
  (make-meta {:model-nb           2
              :attrs-per-model-nb 3}) => {:attributes ["a1" "a2" "a3"]
                                          :models     ["m1" "m2"]})

(defn all-file
  [args]
  (let [{:keys [attributes models]}
        (make-meta args)]
    (attr-file    attributes)
    (model-file   models attributes)
    (content-file models attributes (:content-nb args))))

(comment "A small data set"
  (all-file {:model-nb           2
             :attrs-per-model-nb 3
             :content-nb         10}))

(comment "A 'SID-like' data set"
  (all-file {:model-nb           2000
             :attrs-per-model-nb 450
             :content-nb         130000}))

(println "--------- END OF IMPORT_GEN  ----------" (java.util.Date.))

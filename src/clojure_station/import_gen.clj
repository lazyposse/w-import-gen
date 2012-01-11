(ns ^{:doc "genereate import files"}
  clojure-station.wikeo.import-gen
  (:use     [midje.sweet])
  (:use     [clojure.pprint :only [pp pprint]])
  (:require     [clojure.walk :as w])
  (:require [clojure.set                       :as set])
  (:require [clojure.zip                       :as z])
  (:require [clojure.java.shell :as shell])
  (:import (java.util Date)))


(println "--------- BEGIN OF 4CLOJURE  ----------" (java.util.Date.))

(defn as-lines [s] (reduce #(str %1 %2 "\n")
                           ""
                           s))

(fact (as-lines [1 2]) => "1\n2\n")

(defn spit-as-lines "Given a seq and a filename, write it to the specified file as lines."
  [f s] (spit f (as-lines s)))

(fact "spit-as-lines"
      (let [filename "/tmp/spit-as-lines.txt"]
        (spit-as-lines filename [1 2])   => nil
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
  [] (spit-as-lines "/tmp/attr.csv"
                      (cons (attr-head)
                            (map attr-line (range 10)))))

(defn- model-head
  [] "\"ACTION\";\"TYPE D'OBJET\";\"CODE DU MODELE\";\"SOURCE\";\"TYPE DE MODELE\";\"NOM\";\"NOM D'AFFICHAGE\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";\"MAL\";")

(defn- model-line
  [code nb attribut-code] (str "\"CREATE\";\"Model\";\"" code "\";\"SID\";\"Product\";\"name-" code "\";\"display-name-" code "\";"
                               (reduce str (mapcat (fn [i] (str "\"" i "|true|false|\";"))
                                                   (take nb attribut-code)))))

(fact "model-line"
      (model-line "ProduitRosier" 0 ["reference" "designation" "foo"]) =>
      "\"CREATE\";\"Model\";\"ProduitRosier\";\"SID\";\"Product\";\"name-ProduitRosier\";\"display-name-ProduitRosier\";")

(fact "model-line"
      (model-line "ProduitRosier" 2 ["reference" "designation" "foo"]) =>
      "\"CREATE\";\"Model\";\"ProduitRosier\";\"SID\";\"Product\";\"name-ProduitRosier\";\"display-name-ProduitRosier\";\"reference|true|false|\";\"designation|true|false|\";")

(defn model-file
  [] (spit-as-lines "/tmp/model.csv"
                      (cons (model-head)
                            (map #(model-line % 1 [%]) (range 10)))))



(println "--------- END OF 4CLOJURE  ----------" (java.util.Date.))

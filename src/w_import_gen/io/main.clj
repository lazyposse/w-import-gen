(ns ^{:doc "The sole purpose of this ns is to expose the application
  via a main fn. We need this because this is where Midje compilation
  will be disabled and we don't want that anywhere else."}
  w-import-gen.main
  (:use     [midje.sweet])
  (:require [midje.semi-sweet :as ss])
  (:use     [clojure.tools.cli])
  (:use     [clojure.pprint :only [pp pprint]])
  (:require [clojure.walk :as w])
  (:require [clojure.set  :as set])
  (:require [clojure.zip  :as z])
  (:require [clojure.java.shell :as shell])
  (:require [clojure.java.io :as io])
  (:require [w-import-gen.core :as c])
  (:import (java.util Date))
  (:gen-class))

;; Disable Midje compilation from prod code
(alter-var-root #'*include-midje-checks* (constantly true))

(defn -main "Entry point of the app"
  [& args]
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
    (c/all-file {:model-nb           (options :models)
               :attrs-per-model-nb (options :attributes)
               :content-nb         (options :contents)}   )
    (println "done!")))    

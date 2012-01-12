(ns ^{:doc "Generate import files - io namespace"}
  w_import_gen.import_gen_io
  (:use [w_import_gen.import_gen])
  (:use [midje.sweet])
  (:require [clojure.java.shell :as shell])
  (:require [clojure.java.io :as io])
  (:use [clojure.tools.cli])
  (:gen-class))

(println "--------- BEGIN OF IO  ----------" (java.util.Date.))

(defn lazy-write-lines "Take a seq and a filename and lazily write the seq to the file, each element being on a separate line"
  [f s] (with-open [w (io/writer f)]
          (binding [*out* w]
            (doseq [l s]
              (println l)))))

(fact "lazy-write-lines"
      (let [filename "/tmp/lazy-write-lines.txt"]
        (lazy-write-lines filename [1 2])   => nil
        (:out (shell/sh "cat" filename)) => "1\n2\n"))

(defn attr-file "Output a file of attribute import file"
  [attr-codes] (lazy-write-lines "/tmp/attr.csv"
                      (cons (attr-head)
                            (map attr-line attr-codes))))

(defn model-file "Given a seq of model codes and a seq of model attributes, Write a model import file"
  [models attrs] (lazy-write-lines
                  "/tmp/model.csv"
                  (cons (model-head)
                        (map (fn [model-code]
                               (model-line model-code
                                           (count attrs)
                                           attrs ))
                             models))))

(defn content-file "Given a seq of model codes, a seq of attributes code an a number of content to produce, write a content import file"
  [models attrs content-nb]
  (lazy-write-lines "/tmp/content.csv"
                    (cons (content-head)
                          (take content-nb
                                (cycle
                                 (map (fn [model-code]
                                        (content-line model-code (count attrs) attrs))
                                      models))))))

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

(println "--------- END OF IO  ----------" (java.util.Date.))

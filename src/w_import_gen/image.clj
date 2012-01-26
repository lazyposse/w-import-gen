(ns ^{:doc "Remove unecessary images"}
  w-import-gen.image
  (:use [midje.sweet]
        [clojure-csv.core :only [parse-csv]])
  (:require [  w-import-gen.io.util :as u]))

(unfinished filter-img read-content! )

(defn get-img-id [l]
  (nth (.split l ";") 2))

(fact "get-img-id"
      (get-img-id "a;b;c;d...") => "c")

(defn read-img! [img-file]
  (map get-img-id (u/read-lines img-file)))

(fact "read-img!"
      (read-img! :img-file) => [:id]
      (provided
       (u/read-lines :img-file) => [:line]
       (get-img-id :line) => :id))



(defn filter-img!
  [img-file content-file out-file] (let [img (read-img! img-file)
                                    content (read-content! content-file)]
                                (u/lazy-write-lines out-file (filter-img img content))))

(fact
 (filter-img! :img-file :content-file :out-file) => nil
 (provided
  (read-img! :img-file)                => :img
  (read-content! :content-file)        => :content
  (filter-img :img :content)           => :filtered-lines
  (u/lazy-write-lines :out-file :filtered-lines) => nil))

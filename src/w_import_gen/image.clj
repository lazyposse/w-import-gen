(ns ^{:doc "Remove unecessary images"}
  w-import-gen.image
  (:use [midje.sweet]
        [clojure-csv.core :only [parse-csv]]
        [clojure.repl :only [doc]])
  (:require [w-import-gen.io.util :as u]))

(unfinished filter-img )

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

(defn read-content!
  [content-file]
  (set (mapcat #(get-img-id %) (u/read-lines content-file))))

(fact "read-content!"
  (read-content! :content-file) => #{:img-id1 :img-id2 :img-id3}
  (provided
    (u/read-lines :content-file) => [:line1 :line2]
    (get-img-id :line1) => #{:img-id1 :img-id2}
    (get-img-id :line2) => #{:img-id3}))

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

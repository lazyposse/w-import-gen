(ns ^{:doc "The sole purpose of this ns is to expose the application
  via a main fn. We need this because this is where Midje compilation
  will be disabled and we don't want that anywhere else."}
  w-import-gen.io.main
  (:use     [midje.sweet])
  (:require [midje.semi-sweet :as ss])
  (:use     [clojure.tools.cli])
  (:use     [clojure.pprint :only [pp pprint]])
  (:require [clojure.walk :as w])
  (:require [w-import-gen.io.util :as u])
  (:require [clojure.set  :as set])
  (:require [clojure.zip  :as z])
  (:require [clojure.java.shell :as shell])
  (:require [clojure.java.io :as io])
  (:import (java.util Date))
  #_(:gen-class))

;; Disable Midje compilation from prod code
(alter-var-root #'*include-midje-checks* (constantly true))

(defn -main "Entry point of the app"
  [& args] (u/-main))    

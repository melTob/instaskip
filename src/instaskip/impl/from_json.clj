(ns instaskip.impl.from-json
  (:require [clojure.data.json :as json]
            [clojure.string :refer [join]]))


(def ^:private arrow "\n   -> ")

(defn- ^{:testable true} eskip-json-to-clj [eskip-json] (json/read-str eskip-json))

(defn- eskip-name [eskip-map] (eskip-map "name"))

(defn- is-not-regex? [arg]
  (not
    (and
      (.startsWith arg "/^")
      (.endsWith arg "$/"))))

(defn- arg-to-type [arg]
  (if (and (instance? String arg) (is-not-regex? arg))
    (str "\"" arg "\"")
    arg))

(defn- arguments [args]
  (str "(" (join ", " (map arg-to-type args)) ")"))

(defn- ^{:testable true} predicates [eskip-map]
  (let [preds (eskip-map "predicates")]
    (if (empty? preds)
      "*"
      (join " && "
            (map (fn [predicate]
                   (let [name (eskip-name predicate)]
                     (str name
                          (if (not= name "*")
                            (arguments
                              (predicate "args")))))) preds)))))

(defn- ^{:testable true} filters [eskip-map]
  (let [filters (eskip-map "filters")]
    (join arrow
          (map (fn [filter]
                 (let [name (eskip-name filter)]
                   (str name
                        (arguments
                          (filter "args"))))) filters))))

(defn- ^{:testable true} endpoint [eskip-map]
  (let [endpoint (eskip-map "endpoint")]
    (if (empty? endpoint)
      (str arrow "<shunt>")
      (str arrow "\"" endpoint "\""))))

(defn- single-json->eskip [eskip-map]
  (str (eskip-name eskip-map)
       ": "
       (join arrow
             (filter #(not-empty %) [(predicates eskip-map)
                                     (filters eskip-map)]))
       (endpoint eskip-map)
       ";"))

(defmulti ^:private multi-json->eskip
          "Transforms a clojure map or array into an eskip routes string"
          class)

(defmethod ^:private multi-json->eskip clojure.lang.PersistentVector
  [eskip-json]
  (join "\n\n" (map single-json->eskip eskip-json)))

(defmethod ^:private multi-json->eskip clojure.lang.PersistentArrayMap
  [eskip-json]
  (single-json->eskip eskip-json))

(defn json->eskip
  "Transforms a json string into an eskip routes string"
  [eskip-json]

  (let [eskip-map-or-vector (eskip-json-to-clj eskip-json)]
    (multi-json->eskip eskip-map-or-vector)))
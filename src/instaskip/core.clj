(ns instaskip.core
  (:require [instaparse.core :as insta]
            [clojure.data.json :as json])
  (:gen-class))

(def ^:private eskip-routes-parser
  "returns an parser for eskip routes"
  (insta/parser
    "
     routes           = route+
     route            = name <':'> predicates <'->'> filters endpoint <';'>
     name             = #'[a-zA-Z0-9]+'
     filters          = (filter <'->'> )*
     predicates       = predicate (<'&&'> predicate)* | star
     predicate        = predicate-name <'('> predicate-arg? (<','> predicate-arg)* <')'>
     predicate-name   = #'[a-zA-Z0-9]*'
     predicate-arg    = string | number | regexval
     filter           = filter-name <'('> filter-arg? (<','> filter-arg)* <')'>
     filter-name      = #'[a-zA-Z0-9]*'
     filter-arg       = string | number
     endpoint         = string | shunt
     shunt            = #'<shunt>'
     star             = #'\\*'
     string           = <quote> #'[^\"]*' <quote>
     quote            = #'\"'
     number           = #'[0-9]+\\.[0-9]*'
     regexval         = #'\\/\\^[^$]*\\$/'
     "
    :auto-whitespace :standard
    :output-format :hiccup))

(defn- str->num
  "Transforms a string to a number"
  [str]

  (let [n (read-string str)]
    (if (number? n) n nil)))

(defn- name-and-args-to-map [name & args] {:name name :args (vec args)})
(defn- args-to-map [name & values] (vec `(~name ~(vec values))))

(defn- transform-ast-to-map
  "Tranforms the AST returned after parsing eskip routes to a clojure map"
  [ast]

  (insta/transform
    {:shunt          (fn [_] "")
     :string         identity
     :star           (fn [star] {:name star :args []})
     :number         str->num
     :regexval       identity
     :filter-name    identity
     :filter-arg     identity
     :filter         name-and-args-to-map
     :filters        (partial args-to-map :filters)
     :predicate-name identity
     :predicate-arg  identity
     :predicate      name-and-args-to-map
     :predicates     (partial args-to-map :predicates)
     :route          (fn [& args] (into {} (concat args)))
     :routes         (fn [& args] (vec (concat args)))
     }
     ast))

(defn eskip->json
  "Transforms an eskip routes string to a json string"
  [eskip-routes]

  (json/write-str
    (transform-ast-to-map
      (eskip-routes-parser eskip-routes))))

(def ^:private sample-eskip-routes "
                   hello: predicate1(/^.*$/) && predicate2(\"arg1\", 4.3)
                   -> filter1(\"arg1\")
                   -> filter2(\"arg1\", 4.3, \"arg2\")
                   -> filter3()
                   -> \"https://hello.com\";
                   hello1: pred1(\"hello\") -> <shunt>;
                   hello2: * -> <shunt>;")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (println "Hello, Instaskip!")

  (transform-ast-to-map
    (eskip-routes-parser sample-eskip-routes))

  (print
    (eskip->json sample-eskip-routes))

  ;(insta/visualize
  ;  (eskip eskip-route))
  )

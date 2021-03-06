(ns instaskip.impl.from-eskip-facts
  (:require [midje.sweet :refer :all]
            [instaskip.impl.from-eskip :refer :all]
            [midje.util :refer [testable-privates]]))

(testable-privates instaskip.impl.from-eskip
                   name-and-args-to-map
                   eskip-routes-parser)

(facts "eskip-routes-parser"
       (fact "parses a simple route"
             (eskip-routes-parser "hello1: Host(/^(m-it[.]release[.]site[.]net)$/) -> <shunt>;") =>
             [:routes
              [:route
               [:name "hello1"]
               [:predicates
                [:predicate
                 [:predicate-name "Host"]
                 [:predicate-arg
                  [:regexval "/^(m-it[.]release[.]site[.]net)$/"]]]]
               [:filters] [:endpoint [:shunt "<shunt>"]]]]))

(facts "name-and-args-to-map"
       (fact "parses a name with args"
             (name-and-args-to-map "name"
                                   {:value "arg1" :type "string"}
                                   {:value "0.2" :type "number"}) =>
             {:name "name" :args [{:value "arg1" :type "string"}
                                  {:value "0.2" :type "number"}]}))

(facts "eskip->ast-map"
       (fact "parses a simple route"
             (eskip->maps "hello1: pred1() && pred2(\"Hello\") -> filter1(\"hello\") -> <shunt>;") =>
             [{:name       "hello1",
               :filters    [{:name "filter1" :args [{:value "hello" :type "string"}]}],
               :predicates [{:name "pred1" :args []}
                            {:name "pred2" :args [{:value "Hello" :type "string"}]}]
               :endpoint   ""}]))

(facts "eskip->json"
       (fact "parses a match all route"
             (eskip->json "hello1: * -> <shunt>;") =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"*\",\"args\":[]}],"
                  "\"filters\":[],"
                  "\"endpoint\":\"\"}]"))

       (fact "parses a simple route"
             (eskip->json "hello1: pred1() -> <shunt>;") =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
                  "\"filters\":[],"
                  "\"endpoint\":\"\"}]"))

       (fact "parses a simple route wiht an endpoint"
             (eskip->json "hello1: pred1() -> \"http://www.hello.com/\";") =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
                  "\"filters\":[],"
                  "\"endpoint\":\"http:\\/\\/www.hello.com\\/\"}]"))

       (fact "parses a route with a predicate"
             (eskip->json "hello1: pred1(\"hello\", 3, 4.2, 0, /^.*$/) -> <shunt>;") =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"pred1\","
                  "\"args\":[{\"value\":\"hello\",\"type\":\"string\"},{\"value\":\"3\",\"type\":\"number\"},{\"value\":\"4.2\",\"type\":\"number\"},{\"value\":\"0\",\"type\":\"number\"},{\"value\":\"\\/^.*$\\/\",\"type\":\"regex\"}]}],"
                  "\"filters\":[],"
                  "\"endpoint\":\"\"}]"))

       (fact "parses a route with many predicates"
             (eskip->json "hello1: pred1(\"hello\") && pred2(2) -> <shunt>;") =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"pred1\","
                  "\"args\":[{\"value\":\"hello\",\"type\":\"string\"}]},"
                  "{\"name\":\"pred2\",\"args\":[{\"value\":\"2\",\"type\":\"number\"}]}],"
                  "\"filters\":[],"
                  "\"endpoint\":\"\"}]"))

       (fact "parses a route with many filters"
             (eskip->json "hello1: pred1() -> filter1(\"hello\") -> filter2(2) -> <shunt>;") =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
                  "\"filters\":[{\"name\":\"filter1\","
                  "\"args\":[{\"value\":\"hello\",\"type\":\"string\"}]},"
                  "{\"name\":\"filter2\","
                  "\"args\":[{\"value\":\"2\",\"type\":\"number\"}]}],"
                  "\"endpoint\":\"\"}]"))

       (fact "parses more routes"
             (eskip->json (str "hello1: pred1() -> <shunt>;"
                               "hello2: * -> \"http://hello.com\";")) =>
             (str "[{\"name\":\"hello1\","
                  "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
                  "\"filters\":[],\"endpoint\":\"\"},"
                  "{\"name\":\"hello2\","
                  "\"predicates\":[{\"name\":\"*\",\"args\":[]}],"
                  "\"filters\":[],"
                  "\"endpoint\":\"http:\\/\\/hello.com\"}]")))

(fact "single-eskip->json parses a simple route"
      (single-eskip->json "hello1: pred1() -> <shunt>;") =>
      (str "{\"name\":\"hello1\","
           "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
           "\"filters\":[],"
           "\"endpoint\":\"\"}"))

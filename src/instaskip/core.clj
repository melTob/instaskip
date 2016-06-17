(ns instaskip.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [defun :refer [defun]])
  (:gen-class :main true))

(def ^{:private true} cli-options
  [["-u" "--url URL" "The url for innkeeper" :default "http://localhost:9080"]
   ["-t" "--token TOKEN" "The OAuth token"]
   ["-d" "--dir DIR" "The directory with the eskip files. For action: migrate-routes"]
   ["-h" "--help" "Displays this" :default false]])

(defn- exit [status msg]
  (println msg)
  ;(System/exit status)
  )

(defn- usage [options-summary]
  (->> ["This is the innkeeper cli."
        ""
        "Usage: instaskip [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  migrate-routes  Migrates the routes from an eskip directory to innkeeper"
        "  list-paths      Lists the paths for the current team"
        "  list-routes     Lists the routes for the current team"
        ""
        "Please refer to the manual page for more information. (Which we don't have right now :P)"]
       (string/join \newline)))

(defn- migrate-routes [dir url token]
  (println "Migrate routes. Eskip dir: " dir " Under construction."))

(defn- validate-migrate-routes [opts url token]
  (match opts
         {:options {:dir dir}} (migrate-routes dir url token)
         :else (exit 1 "Invalid options for migrate-routes")))

(defn- list-paths [opts url token]
  (println "List paths. Under construction."))

(defn- list-routes [opts url token]
  (println "List routes. Under construction."))

(defn- parse-action [opts url token]
  (match opts
         {:arguments ["migrate-routes"]} (validate-migrate-routes opts url token)
         {:arguments ["list-paths"]} (list-paths opts url token)
         {:arguments ["list-routes"]} (list-routes opts url token)
         :else (exit 1 "Invalid action")))

(defn -main
  "The application's main function"
  [& args]
  (let [opts (parse-opts args cli-options :in-order false)]
    (log/info opts)
    (match opts
           {:options {:help true}} (exit 0 (usage (opts :summary)))
           {:options {:url url :token token}} (parse-action opts url token)
           :else (exit 1 "Invalid options"))))


;(parse-opts ["--token=\"ttt\"" "list-paths"] cli-options :in-order false)


(comment (-main "-h")
         (-main "--token" "token" "invalid")
         (-main "--token" "token" "list-paths")
         (-main "--token" "token" "list-routes")
         (-main "--token" "token" "migrate-routes")
         (-main "--token" "token" "migrate-routes" "-d" "/eskip-routes"))
;(-main "list-paths --token=\"ttt\"")
;(-main "list-paths --url=http://localhost:8080/ --token=token-user~1-employees-route.write")
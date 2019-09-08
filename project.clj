(defproject approve-transactions "0.1.0-SNAPSHOT"
  :description "A service to check if a transaction can be approved"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-time "0.15.2"]
                 [org.clojure/tools.logging "0.5.0"]
                 [http-kit "2.3.0"]
                 [metosin/compojure-api "2.0.0-alpha30"]]
  :main approve-transactions.main
  :aot [approve-transactions.main]
  :plugins [[lein-midje "3.2.1"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [javax.servlet/javax.servlet-api "3.1.0"]
                                  [ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all}})

(defproject approve-transactions "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-time "0.15.2"]
                 [com.stuartsierra/component "0.4.0"]
                 [http-kit "2.3.0"]
                 [metosin/compojure-api "2.0.0-alpha30"]]
  :ring {:handler approve-transactions.server/app}
  :main approve-transactions.main
  :plugins [[lein-midje "3.2.1"]
            [lein-ring "0.12.5"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [javax.servlet/javax.servlet-api "3.1.0"]
                                  [ring/ring-mock "0.3.2"]]}}
  :uberjar-name "server.jar")

(defproject approve-transactions "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-time "0.15.2"]]
  :plugins [[lein-midje "3.2.1"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]]}}
  :repl-options {:init-ns approve-transactions.core})

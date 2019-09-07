(ns approve-transactions.server
  (:require [approve-transactions.router :as router]
            [org.httpkit.server :as httpkit]
            [compojure.api.sweet :refer :all]
            [com.stuartsierra.component :as component]))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Approve Transactions API"
                    :description "An api that checks if a transaction can be approved."}
             :tags [{:name "api"}]}}}
    (router/app-routes)))

(defn start-server [port]
  (httpkit/run-server
    app
    {:port (read-string port)}))

(defrecord HTTPServer [port]
  component/Lifecycle
  
  (start [component]
    (println "creating server")
    (assoc component :server (start-server port)))
  (stop [component]
    (println "Stopping server")
    (assoc component :server nil)))

(defn new-server [port]
  (map->HTTPServer {:port port}))

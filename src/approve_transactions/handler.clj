(ns approve-transactions.handler
  (:require [compojure.api.sweet :refer :all]
            [org.httpkit.server :as httpkit]
            [ring.util.http-response :refer :all]
            [approve-transactions.controller :as controller]
            [approve-transactions.schema :refer :all]))

(defn app-routes []
  (routes
    (GET "/healthcheck" []
      :return {:msg String}
      :summary "simple health check"
      (ok {:msg "ok"}))

    (POST "/check-transaction" []
      :return CheckTransactionResponse 
      :body [check-transaction-request CheckTransactionRequest]
      :summary "check if transaction is approved following some rules"
      (ok (controller/check-transaction (:account check-transaction-request)
                                        (:transaction check-transaction-request)
                                        (:last-transactions check-transaction-request))))))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Approve Transactions API"
                    :description "An api that checks if a transaction can be approved."}
             :tags [{:name "api"}]}}}
    (app-routes)))

(defn start-server [config]
  (httpkit/run-server app config)
  (printf "server started on http://%s:%s" (:ip config) (:port config)))

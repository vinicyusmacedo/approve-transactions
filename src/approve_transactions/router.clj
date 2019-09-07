(ns approve-transactions.router
  (:require [compojure.api.sweet :refer :all]
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

(ns approve-transactions.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [approve-transactions.logic :as logic]))

(s/defschema Pizza
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})

(s/defschema Account
  {:card-is-active s/Bool
   :limit          s/Num
   :denylist       [s/Str]})

(s/defschema Transaction
  {:merchant s/Str
   :amount   s/Num
   :time     s/Str})

(s/defschema CheckTransactionRequest
  {:account           Account
   :transaction       Transaction
   :last-transactions [Transaction]})

(s/defschema CheckTransactionResponse
  {:approved       s/Bool
   :new-limit      s/Num
   :denied-reasons [s/Str]})

; TODO - move to controller
(defn check-transaction [check-transaction-request]
  (logic/can-transaction-be-authorized? (:account check-transaction-request)
                                  (:transaction check-transaction-request)
                                  (:last-transactions check-transaction-request)))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Approve Transactions API"
                    :description "An api that checks if a transaction can be approved."}
             :tags [{:name "api"}]}}}

    (context "/api" []
      :tags ["api"]

      (GET "/healthcheck" []
        :return {:msg String}
        :summary "simple health check"
        (ok {:msg "ok"}))

      (POST "/check-transaction" []
        :return CheckTransactionResponse 
        :body [check-transaction-request CheckTransactionRequest]
        :summary "check if transaction is approved following some rules"
        (ok (check-transaction check-transaction-request))))))

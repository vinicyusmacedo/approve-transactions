(ns approve-transactions.handler-test
  (:require [cheshire.core :as cheshire]
            [midje.sweet :refer :all]
            [approve-transactions.handler :refer :all]
            [ring.mock.request :as mock]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(fact "Test healthcheck"
  (let [response (app (-> (mock/request :get "/healthcheck")))
        body     (parse-body (:body response))]
    (:status response) => 200
    (:msg body) => "ok"))

(def test-payload
  {:account {
     :cardIsActive true
     :limit 1000.00
     :denylist []}
   :transaction {
     :merchant "Stores Ltd."
     :amount 100.00
     :time "2019-08-25T10:00:00.00Z"}
   :lastTransactions []})

(def test-payload-response
  {:approved true
   :newLimit 900.00
   :deniedReasons []})

(fact "Test check transaction"
  (let [response (app (-> (mock/request :post "/check-transaction")
                          (mock/content-type "application/json")
                          (mock/body (cheshire/generate-string test-payload))))
        body     (parse-body (:body response))]
    (:status response) => 200
    body => test-payload-response))

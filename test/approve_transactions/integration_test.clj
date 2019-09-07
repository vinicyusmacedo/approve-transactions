(ns approve-transactions.integration-test
  (:require [cheshire.core :as cheshire]
            [midje.sweet :refer :all]
            [approve-transactions.handler :refer :all]
            [ring.mock.request :as mock]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn make-request [account transaction last-transactions]
  (let [test-payload {:account account
                      :transaction transaction
                      :last-transactions last-transactions}]
    (app (-> (mock/request :post "/check-transaction")
             (mock/content-type "application/json")
             (mock/body (cheshire/generate-string test-payload))))))

(def first-transaction-denied-reasons ["card blocked"
                                       "transaction above limit for first transaction"
                                       "merchant in denylist"])

(def bad-transaction-denied-reasons ["card blocked"
                                     "transaction above limit"
                                     "merchant in denylist"])

(def bad-transaction-rate-limit-denied-reasons ["card blocked"
                                                "transaction above limit"
                                                "merchant in denylist"
                                                "transaction rate limit exceeded"])

(def bad-transaction-merchant-limit-denied-reasons ["card blocked"
                                                    "transaction above limit"
                                                    "merchant in denylist"
                                                    "merchant limit exceeded"
                                                    "transaction rate limit exceeded"])

(def ok-account
  {:card-is-active true
   :limit 1000.00
   :denylist []})

(def bad-account
  {:card-is-active false
   :limit 1000.00
   :denylist ["Not Stores Ltd."]})

(def ok-transaction
  {:merchant "Stores Ltd."
   :amount 100.00
   :time "2019-08-25T10:01:59.00Z"})

(def bad-transaction
  {:merchant "Not Stores Ltd."
   :amount 1000.01
   :time "2019-08-25T10:01:59.00Z"})

(def bad-first-transaction
  {:merchant "Not Stores Ltd."
   :amount 900.01
   :time "2019-08-25T10:01:59.00Z"})

(def bad-repeated-transaction
  {:merchant "Not Stores Ltd."
   :amount 100.00
   :time "2019-08-25T10:00:00.00Z"})

(def bad-last-transactions-rate-limit (repeat 3 bad-repeated-transaction))

(def bad-last-transactions-merchant-limit (repeat 10 bad-repeated-transaction))

(fact "Test ok transaction"
  (let [response (make-request ok-account ok-transaction (repeat 1 ok-transaction))
        body     (parse-body (:body response))]
    (:status response) => 200
    body => {:approved true
             :new-limit 900.00
             :denied-reasons []}))

(fact "Test ok first transaction"
  (let [response (make-request ok-account ok-transaction [])
        body     (parse-body (:body response))]
    (:status response) => 200
    body => {:approved true
             :new-limit 900.00
             :denied-reasons []}))

(fact "Test bad first transaction"
  (let [response (make-request bad-account bad-first-transaction [])
        body     (parse-body (:body response))]
    (:status response) => 200
    body => {:approved false
             :new-limit 1000.00
             :denied-reasons first-transaction-denied-reasons}))

(fact "Test bad transaction"
  (let [response (make-request bad-account bad-transaction (repeat 1 bad-transaction))
        body     (parse-body (:body response))]
    (:status response) => 200
    body => {:approved false
             :new-limit 1000.00
             :denied-reasons bad-transaction-denied-reasons}))

(fact "Test bad transaction - hit rate limit"
  (let [response (make-request bad-account bad-transaction bad-last-transactions-rate-limit)
        body     (parse-body (:body response))]
    (:status response) => 200
    body => {:approved false
             :new-limit 1000.00
             :denied-reasons bad-transaction-rate-limit-denied-reasons}))

(fact "Test bad transaction - hit rate limit and merchant limit"
  (let [response (make-request bad-account bad-transaction bad-last-transactions-merchant-limit)
        body     (parse-body (:body response))]
    (:status response) => 200
    body => {:approved false
             :new-limit 1000.00
             :denied-reasons bad-transaction-merchant-limit-denied-reasons}))

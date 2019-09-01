(ns approve-transactions.controller-test
  (:require [approve-transactions.controller :as controller]
            [midje.sweet :refer :all]))

(def account {:card-is-active true
              :limit 1000.00
              :denylist []})

(def bad-account {:card-is-active false
                  :limit 1000.00
                  :denylist ["Stores Ltd."]})

(def transaction {:merchant "Stores Ltd."
                  :amount 100.00
                  :time "2019-08-25T10:00:00.00Z"})

(def bad-transaction {:merchant "Stores Ltd."
                      :amount 1000.01
                      :time "2019-08-25T10:00:00.00Z"})

(def denied-reasons ["card blocked"
                     "transaction above limit"
                     "transaction above limit for first transaction"
                     "merchant in denylist"
                     "merchant limit exceeded"
                     "transaction rate limit exceeded"])

(def bad-transaction-response {:approved false
                               :new-limit 1000.00
                               :denied-reasons (remove #{"transaction above limit for first transaction"} denied-reasons)})

(def bad-first-transaction-response {:approved false
                                     :new-limit 1000.00
                                     :denied-reasons (remove #{"merchant limit exceeded"
                                                               "transaction rate limit exceeded"}
                                                             denied-reasons)})


(def ok-transaction-response {:approved true
                              :new-limit 900.00
                              :denied-reasons []})

(fact "An OK first transaction" (controller/check-transaction account transaction []) => ok-transaction-response)
(fact "An OK transaction" (controller/check-transaction account transaction (repeat 2 transaction)) => ok-transaction-response)
(fact "A bad first transaction" (controller/check-transaction bad-account bad-transaction []) => bad-first-transaction-response)
(fact "A bad transaction" (controller/check-transaction bad-account bad-transaction (repeat 10 bad-transaction)) => bad-transaction-response)

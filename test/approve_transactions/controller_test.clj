(ns approve-transactions.controller-test
  (:require [approve-transactions.controller :as controller]
            [midje.sweet :refer :all]))

(def account {:card-is-active true
              :limit 1000.00
              :allow-listed false
              :denylist []})

(def account-allow-listed {:card-is-active true
                           :limit 1000.00
                           :allow-listed true
                           :denylist ["Stores Ltd."]})

(def bad-account-allow-listed {:card-is-active false
                               :limit 1000.00
                               :allow-listed true
                               :denylist ["Stores Ltd."]})

(def bad-account {:card-is-active false
                  :limit 1000.00
                  :allow-listed false
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

(def denied-reasons-allow-listed ["card blocked"
                                  "transaction above limit"])

(def bad-transaction-response {:approved false
                               :new-limit 1000.00
                               :allow-listed false
                               :denied-reasons (remove #{"transaction above limit for first transaction"} denied-reasons)})

(def bad-transaction-turned-ok-response {:approved true
                                         :new-limit 900.00
                                         :allow-listed true
                                         :denied-reasons []})

(def bad-transaction-allow-listed-response {:approved false
                                            :new-limit 1000.00
                                            :allow-listed true
                                            :denied-reasons denied-reasons-allow-listed})

(def bad-first-transaction-response {:approved false
                                     :new-limit 1000.00
                                     :allow-listed false
                                     :denied-reasons (remove #{"merchant limit exceeded"
                                                               "transaction rate limit exceeded"}
                                                             denied-reasons)})


(def ok-transaction-response {:approved true
                              :new-limit 900.00
                              :allow-listed false
                              :denied-reasons []})

(fact "An OK first transaction" (controller/check-transaction account transaction []) => ok-transaction-response)
(fact "An OK transaction" (controller/check-transaction account transaction (repeat 2 transaction)) => ok-transaction-response)
(fact "A bad first transaction" (controller/check-transaction bad-account bad-transaction []) => bad-first-transaction-response)
(fact "A bad transaction" (controller/check-transaction bad-account bad-transaction (repeat 10 bad-transaction)) => bad-transaction-response)
(fact "An bad transaction turned ok by allow-listed" (controller/check-transaction account-allow-listed transaction (repeat 10 transaction)) => bad-transaction-turned-ok-response)
(fact "An bad transaction blocked with allow-listed" (controller/check-transaction bad-account-allow-listed bad-transaction (repeat 10 transaction)) => bad-transaction-allow-listed-response)

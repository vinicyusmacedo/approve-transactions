(ns approve-transactions.logic-test
  (:require [midje.sweet :refer :all]
            [approve-transactions.logic :as logic]))

(def account {:card-is-active true
              :limit          1000.00
              :denylist       []})

(def account-with-denylist {:card-is-active false
              :limit          1000.00
              :denylist       ["Stores Ltd."]})

(def account-not-active {:card-is-active false
                         :limit          1000.00
                         :denylist       []})

(def transaction {:merchant "Stores Ltd."
                        :amount   100.00
                        :time     "2019-08-25T10:00:00.00Z"})

(def new-transaction {:merchant "Not Stores Ltd." :amount   100.00
                       :time     "2019-08-25T10:00:00.00Z"})

(def bad-transaction {:merchant "Stores Ltd." :amount   100.00
                       :time     "2019-08-25T10:00:00.00Z"})

(def last-transactions [transaction])

(def last-transactions-rate-limit (repeat 10 transaction))

(fact "First transaction"
  (logic/is-first-transaction? nil) => true)

(fact "Not the first transaction"
  (logic/is-first-transaction? (repeat 2 last-transaction)) => false)

(fact "Below account limit"
  (logic/is-below-limit? account 1000.00) => true)

(fact "Above account limit"
  (logic/is-below-limit? account 1000.01) => false)

(fact "Below 90% of account limit"
  (logic/is-below-90percent-limit? account 900.00) => true)

(fact "Above 90% of account limit"
  (logic/is-below-90percent-limit? account 900.01) => false)

(fact "Card is active"
  (logic/is-card-active? account) => true)

(fact "Card is not active"
  (logic/is-card-active? account-not-active) => false)

(fact "Merchant above threshold of transactions"
  (logic/is-above-merchant-threshold? "Stores Ltd." (repeat 10 transaction)) => true)

(fact "Merchant below threshold of transactions"
  (logic/is-above-merchant-threshold? "Stores Ltd." (repeat 9 transaction)) => false)

(fact "Rate limit exceeded")

(fact "Rate limit not exceeded")

(fact "Merchant in denylist"
  (logic/is-merchant-not-in-denylist? "Stores Ltd." account-with-denylist) => false)

(fact "Merchant not in denylist"
  (logic/is-merchant-not-in-denylist? "Stores Ltd." account) => true)

(fact "A new limit is calculated"
  (logic/calculate-new-limit account 100.00) => 900.00)

(fact "Complete output for transaction authorization"
  (logic/transaction-authorization-output false 1000.00 ["deny reason 1"
                                                         "deny reason 2"]) => {:approved false
                                                                    :new-limit 1000.00
                                                                    :denied-reasons ["deny reason 1"
                                                                                     "deny reason 2"]})

(fact "An approved transaction (all good)"
  (logic/can-transaction-be-authorized? account transaction last-transactions) => {:approved true
                                                                                   :new-limit 900.00
                                                                                   :denied-reasons []})

(fact "A not-approved transaction"
  (logic/can-transaction-be-authorized? account-with-denylist transaction last-transactions-rate-limit) => {:approved false
                                                                                            :new-limit 1000.00
                                                                                            :denied-reasons ["card blocked"
                                                                                                             "transaction above limit"
                                                                                                             "merchant in denylist"
                                                                                                             "merchant limit exceeded"
                                                                                                             "transaction rate limit exceeded"]})
(fact "A not-approved first transaction"
  (logic/can-transaction-be-authorized? account-with-denylist transaction []) => {:approved false
                                                                                  :new-limit 1000.00
                                                                    :denied-reasons ["transaction above limit for first transaction"
                                                                                     "card blocked"
                                                                                     "transaction above limit"
                                                                                     "merchant in denylist"]})

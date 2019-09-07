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

(def transaction-above-limit {:merchant "Stores Ltd."
                              :amount   1000.01
                              :time     "2019-08-25T10:00:00.00Z"})

(def transaction-above-90percent-limit {:merchant "Stores Ltd."
                                        :amount   900.01 
                                        :time     "2019-08-25T10:00:00.00Z"})

(def new-transaction {:merchant "Not Stores Ltd."
                      :amount   100.00
                      :time     "2019-08-25T10:00:00.00Z"})

(def bad-transaction {:merchant "Stores Ltd."
                      :amount   100.00
                      :time     "2019-08-25T10:00:00.00Z"})

(def last-transactions [transaction])

(def last-transactions-merchant-limit (repeat 10 transaction))

(def last-transactions-rate-limit (repeat 3 transaction))

(fact "First transaction"
  (logic/is-first-transaction? []) => true)

(fact "Not the first transaction"
  (logic/is-first-transaction? (repeat 2 last-transactions)) => false)

(fact "Below account limit"
  (logic/is-above-limit? account 1000.00) => false)

(fact "Above account limit"
  (logic/is-above-limit? account 1000.01) => true)

(fact "Below 90% of account limit"
  (logic/is-above-90percent-limit? account 900.00) => false)

(fact "Above 90% of account limit"
  (logic/is-above-90percent-limit? account 900.01) => true)

(fact "Above limit for first transaction"
  (logic/is-above-limit-for-first-transaction? account [] 900.01) => true)

(fact "Not above limit for first transaction (not first transaction)"
  (logic/is-above-limit-for-first-transaction? account last-transactions 900.01) => false)

(fact "Card is active"
  (logic/is-card-blocked? account) => false)

(fact "Card is not active"
  (logic/is-card-blocked? account-not-active) => true)

(fact "Merchant above threshold of transactions"
  (logic/is-above-merchant-threshold? "Stores Ltd." (repeat 10 transaction)) => true)

(fact "Merchant below threshold of transactions"
  (logic/is-above-merchant-threshold? "Stores Ltd." (repeat 9 transaction)) => false)

(fact "Sorted last transactions by time"
  (logic/sort-last-transactions-by-time [{:time "2019-08-25T10:00:00.00Z"} {:time "2019-08-25T10:02:00.00Z"}]) => [{:time "2019-08-25T10:02:00.00Z"}
                                                                                                                   {:time "2019-08-25T10:00:00.00Z"}])

(fact "Rate limit exceeded"
  (logic/is-rate-limit-exceeded? last-transactions-rate-limit "2019-08-25T10:01:59.00Z") => true)

(fact "Rate limit not exceeded"
  (logic/is-rate-limit-exceeded? last-transactions-rate-limit "2019-08-25T10:02:00.00Z") => false)

(fact "Merchant in denylist"
  (logic/is-merchant-in-denylist? "Stores Ltd." account-with-denylist) => true)

(fact "Merchant not in denylist"
  (logic/is-merchant-in-denylist? "Stores Ltd." account) => false)

(fact "A new limit is calculated"
  (logic/calculate-new-limit account 100.00) => 900.00)

(fact "Should eval a is-first-transaction? function"
  (logic/eval-rule "first transaction" logic/is-first-transaction? []) => "first transaction")

(fact "Should return transaction-rules accordingly"
  (logic/transaction-rules account transaction last-transactions) => [["card blocked" logic/is-card-blocked? account]
                                                                      ["transaction above limit" logic/is-above-limit? account (:amount transaction)]
                                                                      ["transaction above limit for first transaction" logic/is-above-limit-for-first-transaction? account last-transactions (:amount transaction)]
                                                                      ["merchant in denylist" logic/is-merchant-in-denylist? (:merchant transaction) account]
                                                                      ["merchant limit exceeded" logic/is-above-merchant-threshold? (get transaction :merchant) last-transactions]
                                                                      ["transaction rate limit exceeded" logic/is-rate-limit-exceeded? last-transactions (:time transaction)]])

(fact "Complete output for transaction authorization"
  (logic/transaction-authorization-output false 1000.00 ["deny reason 1"
                                                         "deny reason 2"]) => (just {:approved false
                                                                                     :new-limit 1000.00
                                                                                     :denied-reasons ["deny reason 1"
                                                                                                      "deny reason 2"]}))

(fact "An approved transaction (all good)"
  (logic/can-transaction-be-authorized? account transaction last-transactions) => (just {:approved true
                                                                                         :new-limit 900.00
                                                                                         :denied-reasons []}))

(fact "A not-approved transaction"
  (logic/can-transaction-be-authorized? account-with-denylist transaction-above-limit last-transactions-merchant-limit) => (just {:approved false
                                                                                                                                  :new-limit 1000.00
                                                                                                                                  :denied-reasons ["card blocked"
                                                                                                                                                   "transaction above limit"
                                                                                                                                                   "merchant in denylist"
                                                                                                                                                   "merchant limit exceeded"
                                                                                                                                                   "transaction rate limit exceeded"]}))

(fact "A not-approved first transaction"
  (logic/can-transaction-be-authorized? account-with-denylist transaction-above-90percent-limit []) => (just {:approved false
                                                                                                              :new-limit 1000.00
                                                                                                              :denied-reasons ["card blocked"
                                                                                                                               "transaction above limit for first transaction"
                                                                                                                               "merchant in denylist"]}))

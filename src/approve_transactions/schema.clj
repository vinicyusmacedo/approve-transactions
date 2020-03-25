(ns approve-transactions.schema
  (:require [schema.core :as s]))

(s/defschema Account
  {:card-is-active s/Bool
   :limit          s/Num
   :allow-listed   s/Bool
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

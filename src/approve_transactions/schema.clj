(ns approve-transactions.schema
  (:require [schema.core :as s]))

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

(ns approve-transactions.controller
  (:require [approve-transactions.logic :as logic]))

(defn check-transaction [account transaction last-transactions]
  (logic/can-transaction-be-authorized? account transaction last-transactions))

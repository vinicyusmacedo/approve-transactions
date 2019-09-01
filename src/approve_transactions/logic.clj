(ns approve-transactions.logic)

(def ^:const merchant-threshold 10)

(def ^:const first-transaction-percentage 0.10)

(def ^:const rate-limit-mins 2)

(defn transaction-authorization-output [approved new-limit denied-reasons]
  {:approved approved
   :new-limit new-limit
   :denied-reasons denied-reasons})

(defn eval-rule [reason rule & args]
  (if (apply rule args)
    reason))

(defn calculate-new-limit [account amount]
  (- (get account :limit) amount))

(defn is-first-transaction? [last-transactions]
  (empty? last-transactions))

(defn is-above-limit? [account amount]
  (let [limit (get account :limit)]
    (< limit amount)))

(defn is-above-90percent-limit? [account amount]
  (let [limit (get account :limit)
        limit-percent (- limit (* limit first-transaction-percentage))]
    (< limit-percent amount)))

(defn is-above-limit-for-first-transaction? [account last-transactions amount]
  (if (is-first-transaction? last-transactions)
    (is-above-90percent-limit? account amount)
    false))

(defn is-card-blocked? [account]
  (not (get account :card-is-active)))

(defn is-above-merchant-threshold? [merchant last-transactions]
  (>= (count (filter #(= (:merchant %) merchant) last-transactions)) merchant-threshold))

(defn is-rate-limit-exceeded? [last-transactions timestamp]
  true)

(defn is-merchant-in-denylist? [merchant account]
  (boolean (some #(= merchant %) (get account :denylist))))

(defn transaction-rules [account transaction last-transactions]
  [["card blocked" is-card-blocked? account]
   ["transaction above limit" is-above-limit? account (get transaction :amount)]
   ["transaction above limit for first transaction" is-above-limit-for-first-transaction? account last-transactions (get transaction :amount)]
   ["merchant in denylist" is-merchant-in-denylist? (get transaction :merchant) account]
   ["merchant limit exceeded" is-above-merchant-threshold? (get transaction :merchant) last-transactions]
   ["transaction rate limit exceeded" is-rate-limit-exceeded? last-transactions (get transaction :time)]])

(defn can-transaction-be-authorized? [account transaction last-transactions]
  (let [amount (get transaction :amount)
        denied-reasons (remove nil? (map #(apply eval-rule %) (transaction-rules account transaction last-transactions)))
        transaction-approved (empty? denied-reasons)]
    (transaction-authorization-output transaction-approved 
                                      (if transaction-approved (calculate-new-limit account amount) (get account :limit))
                                      denied-reasons)))

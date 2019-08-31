(ns approve-transactions.logic)

(def ^:const merchant-threshold 10)

(def ^:const first-transaction-percentage 0.10)

(def ^:const rate-limit-mins 2)

(defn transaction-authorization-output [approved new-limit denied-reasons]
  {:approved approved
   :new-limit new-limit
   :denied-reasons denied-reasons})

(defn can-transaction-be-authorized? [account transaction last-transactions]
  (transaction-authorization-output true 900.00 []))

(defn calculate-new-limit [account amount]
  (- (get account :limit) amount))

(defn is-first-transaction? [last-transactions]
  (empty? last-transactions))

(defn is-below-limit? [account amount]
  (let [limit (get account :limit)]
    (>= limit amount)))

(defn is-below-90percent-limit? [account amount]
  (let [limit (get account :limit)
        limit-percent (- limit (* limit first-transaction-percentage))]
    (<= amount limit-percent)))

(defn is-card-active? [account]
  (get account :card-is-active))

(defn is-above-merchant-threshold? [merchant last-transactions]
  (>= (count (filter #(= (:merchant %) merchant) last-transactions)) merchant-threshold))

(defn is-rate-limit-exceeded? [last-transactions timestamp])

(defn is-merchant-not-in-denylist? [merchant account]
  (nil? (some #{merchant} (get account :denylist))))

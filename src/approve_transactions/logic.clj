(ns approve-transactions.logic)

(require '[clj-time.core :as time-core]
         '[clj-time.format :as time-format])

(def merchant-threshold 10)

(def first-transaction-percentage 0.10)

(def rate-limit-mins 2)

(def rate-limit-transactions 3)

(defn transaction-authorization-output [approved new-limit denied-reasons]
  {:approved approved
   :new-limit new-limit
   :denied-reasons denied-reasons})

(defn eval-rule 
  "This function is used to eval rules and return a reason if the rule is evaled as true"
  [reason rule & args]
  (if (apply rule args)
    reason))

(defn calculate-new-limit [account amount]
  (- (:limit account) amount))

(defn is-first-transaction? [last-transactions]
  (empty? last-transactions))

(defn is-above-limit? [account amount]
  (let [limit (:limit account)]
    (< limit amount)))

(defn is-above-90percent-limit? [account amount]
  (let [limit (:limit account)
        limit-percent (- limit (* limit first-transaction-percentage))]
    (< limit-percent amount)))

(defn is-above-limit-for-first-transaction?
  "The first transaction shouldn't be above 90% of the limit, so we check whether a transaction is the first
  and whether it is above limit"
  [account last-transactions amount]
  (if (is-first-transaction? last-transactions)
    (is-above-90percent-limit? account amount)
    false))

(defn is-card-blocked? [account]
  (not (:card-is-active account)))

(defn is-above-merchant-threshold? [merchant last-transactions]
  (>= (count (filter #(= (:merchant %) merchant) last-transactions)) merchant-threshold))

(defn sort-last-transactions-by-time 
  "This function sorts a list of last-transactions by their timestamp"
  [last-transactions]
  (sort-by :time #(compare %2 %1) last-transactions))

(defn is-rate-limit-exceeded?
  "This function:
  - Gets the 3rd item of the array of last-transactions;
  - Sums the time of the 3rd item by 2 minutes
  - Checks if the supplied timestamp is within the two times"
  [last-transactions timestamp]
  (if-let [limit-transaction (nth (sort-last-transactions-by-time last-transactions) (- rate-limit-transactions 1) nil)]
    (let [limit-transaction-start (time-format/parse (:time limit-transaction))
          limit-transaction-end (time-core/plus limit-transaction-start (time-core/minutes rate-limit-mins))
          current-time (time-format/parse timestamp)]
      (time-core/within? (time-core/interval limit-transaction-start limit-transaction-end) current-time))))

(defn is-merchant-in-denylist? [merchant account]
  (boolean (some #(= merchant %) (:denylist account))))

(defn transaction-rules
  "Returns a list of rules and denied reasons"
  [account transaction last-transactions]
  [["card blocked" is-card-blocked? account]
   ["transaction above limit" is-above-limit? account (:amount transaction)]
   ["transaction above limit for first transaction" is-above-limit-for-first-transaction? account last-transactions (:amount transaction)]
   ["merchant in denylist" is-merchant-in-denylist? (:merchant transaction) account]
   ["merchant limit exceeded" is-above-merchant-threshold? (:merchant transaction) last-transactions]
   ["transaction rate limit exceeded" is-rate-limit-exceeded? last-transactions (:time transaction)]])

(defn can-transaction-be-authorized?
  "Checks if a transaction can be authorized by evaluating all rules from transaction-rules
  Returns the new limit, whether the transaction was approved and a list of reasons if the transaction was denied"
  [account transaction last-transactions]
  (let [amount (:amount transaction)
        denied-reasons (remove nil? (map #(apply eval-rule %) (transaction-rules account transaction last-transactions)))
        transaction-approved (empty? denied-reasons)]
    (transaction-authorization-output transaction-approved 
                                      (if transaction-approved (calculate-new-limit account amount) (:limit account))
                                      denied-reasons)))

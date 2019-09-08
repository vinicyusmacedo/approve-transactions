(ns approve-transactions.main
  (:gen-class)
  (:require [approve-transactions.handler :as handler]))

(defn -main []
  (handler/start-server {:port (or (read-string (System/getenv "PORT")) 3000)
                         :ip "0.0.0.0"}))

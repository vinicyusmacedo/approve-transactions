(ns approve-transactions.main
  (:gen-class)
  (:require [approve-transactions.handler :as handler]))

(defn -main [port]
  (handler/start-server {:port (read-string port)
                         :ip "0.0.0.0"}))

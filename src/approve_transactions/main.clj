(ns approve-transactions.main
  (:require [com.stuartsierra.component :as component]
            [approve-transactions.server :as server]))

(defn system-map [config]
  (component/system-map
    :http (server/new-server (:port config))))

(defn start-all [port]
  (def system (component/start (system-map {:port port}))))

(defn stop-all []
  (component/stop system-map))

(defn -main [port]
  (start-all port))

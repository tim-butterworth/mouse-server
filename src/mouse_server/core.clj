(ns mouse-server.core
  (:require [mouse-server.socket-utils.socket :as socket-util]
            [mouse-server.json-utils.json :as json]))

(def sockets (atom []))
(def run-server (atom true))
(def server (atom nil))
(def port (atom 0))

(defn list-methods [obj] 
  (map 
   (fn [method] 
     (. method getName)) 
   (. (. obj getClass) getMethods)))

(defn do-and-return [obj action]
  (do 
    (action obj)
    obj))

(defn close-socket [socket]
  (do-and-return 
   socket 
   (fn [s]
     (socket-util/handle-exception (fn [] (. s close))))))

(defn close-sockets [lst]
  (map 
   close-socket
   lst))

(defn filter-closed-sockets [lst]
  (filter
   socket-util/is-open
   lst))

(defn close-all-sockets []
  (do-and-return 
   sockets
   (fn [lst]
     (swap! lst 
            (fn [lst] 
              (-> lst 
                  (close-sockets)
                  (filter-closed-sockets)))))))

(defn add-socket [socket]
  (do
    (println "adding a socket")
    (-> (close-all-sockets) 
        ((fn [a] (swap! a (fn [n] (conj n socket))))))))

(defn listen-for-writes [socket]
  (future
    (let [reader (socket-util/get-socket-reader socket)]
      (loop []
        (let [line (. reader readLine)]
          (if (not (= nil line))
            (do
              (println line)
              (socket-util/handle-exception (fn []
                                              (println (json/to-mp line))))
              (recur))
            (println "the socket has closed... I am done listening for writes")))))))

(defn listen-for-connections [server]
  (future 
    (loop []
      (if @run-server
        (do
          (println "waiting for a connection...")
          (add-socket (socket-util/wait-for-connection server))
          (recur))
        (socket-util/handle-exception
         (fn [] (. server close))
         "stopped listening")))))

(defn stop-server []
  (reset! run-server false))

(defn start-server [port]
  (socket-util/handle-exception
   (fn []
     (let [server (socket-util/get-server port)]
       (listen-for-connections server)))))

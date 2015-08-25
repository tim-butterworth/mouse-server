(ns mouse-server.core
  (:gen-class))

(import java.net.ServerSocket)
(import java.net.Socket)
(import java.io.InputStream)
(import java.io.InputStreamReader)
(import java.io.BufferedReader)
(import java.io.OutputStream)
(import java.io.OutputStreamWriter)
(import java.io.BufferedWriter)

(def sockets (atom []))
(def run-server (atom true))

(defn log-exception [e]
  (println e))

(defn list-methods [obj] 
  (map 
   (fn [method] 
     (. method getName)) 
   (. (. obj getClass) getMethods)))

(defn do-and-return [obj action]
  (do 
    (action obj)
    obj))

(defn handle-exception [action]
  (try
    (action)
    (catch Exception e
      (log-exception e))))

(defn close-socket [socket]
  (do-and-return 
   socket 
   (fn [s]
     (handle-exception (fn [] (. s close))))))

(defn is-open [socket]
  (not (. socket isClosed)))

(defn close-sockets [lst]
  (map 
   close-socket
   lst))

(defn filter-closed-sockets [lst]
  (filter
   is-open
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

(defn get-server [port]
  (handle-exception (fn [] (ServerSocket. port))))

(defn wait-for-connection [server]
  (. server accept))

(defn add-socket [socket]
  (do
    (println "adding a socket")
    (-> (close-all-sockets) 
        ((fn [a] (swap! a (fn [n] (conj n socket))))))))

(defn stop-server []
  (reset! run-server false))

(defn get-socket-writer [socket]
  (let [out (. socket getOutputStream)
        writer (OutputStreamWriter. out)]
    (BufferedWriter. writer)))

(defn write [message socket]
  (let [out (get-socket-writer socket)]
    (do
      (. out write (str message "\n"))
      (. out flush))))

(defn get-socket-reader [socket]
  (let [in (. socket getInputStream)
        reader (InputStreamReader. in)]
    (BufferedReader. reader)))

(defn listen-for-writes []
  (future
    (loop []
      (do
        ))))

(defn listen-for-connections [server]
  (future 
    (loop []
      (do
        (if @run-server
          (do
            (println "waiting for a connection...")
            (add-socket (wait-for-connection server))
            (recur))
          (do
            (println "stopped listening")
            (try 
              (. server close)
              (catch Exception e
                (println e)))))))))

(defn -main
  [& args]
  (println "Hello, World!"))



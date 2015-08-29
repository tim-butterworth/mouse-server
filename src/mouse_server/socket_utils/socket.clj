(ns mouse-server.socket-utils.socket)

(import java.net.ServerSocket)
(import java.net.Socket)
(import java.io.InputStream)
(import java.io.InputStreamReader)
(import java.io.BufferedReader)
(import java.io.OutputStream)
(import java.io.OutputStreamWriter)
(import java.io.BufferedWriter)

(defn log-exception [e]
  (println e))

(defn log-message [m]
  (println m))

(defn handle-exception 
  ([action message]
   (do
     (log-message message)
     (handle-exception action)))
  ([action]
   (try
     (action)
     (catch Exception e
       (log-exception e)))))

(defn is-open [socket]
  (not (. socket isClosed)))

(defn get-server [port]
  (handle-exception (fn [] (ServerSocket. port))))

(defn wait-for-connection [server]
  (. server accept))

(defn get-socket-writer [socket]
  (let [out (. socket getOutputStream)
        writer (OutputStreamWriter. out)]
    (BufferedWriter. writer)))

(defn get-socket-reader [socket]
  (let [in (. socket getInputStream)
        reader (InputStreamReader. in)]
    (BufferedReader. reader)))

(defn write [socket message]
  (let [out (get-socket-writer socket)]
    (do
      (. out write (str message "\n"))
      (. out flush))))

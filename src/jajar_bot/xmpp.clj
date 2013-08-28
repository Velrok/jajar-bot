(ns jajar-bot.xmpp
  (:import [org.jivesoftware.smack XMPPConnection
                                   SASLAuthentication
                                   ChatManagerListener
                                   MessageListener]
           [org.jivesoftware.smack.packet Message]))


(defn message->map
  "Convert Message object to clojure map."
  [message]
  {:from      (.getFrom message)
   :to        (.getTo message)
   :subject   (.getSubject message)
   :body      (.getBody message)
   :thread    (.getThread message)
   :error     (.getError message)
   :type      (.getType message)})


(def last-message (atom nil))


(defn message-listener [f]
  (proxy [MessageListener] []
    (processMessage [chat message]
      (reset! last-message message)
      (let [answer (f (message->map message))]
        (if answer
          (.sendMessage chat (str answer)))))))


(defn chat-manager-listender [f]
  (proxy [ChatManagerListener] []
    (chatCreated [chat created-localy?]
      (if (not created-localy?)
        (.addMessageListener chat
                             (message-listener f))))))

(def all-connections (atom []))

(defn start-bot
  "Opens a connection to the jabber server.
  The message-handler has to be a function that takes one argument.
  It returns the created connection."
  [host username password message-handler]
  {:pre [(string? host)
         (string? username)
         (string? password)
         (fn? message-handler)]}

  (let [con (XMPPConnection. host)]
    (println "connecting ...")
    (.connect con)
    (.login con username password "bot")
    (println "connected as " username "@" host)
    (-> con
        (.getChatManager)
        (.addChatListener (chat-manager-listender message-handler)))
    (swap! all-connections conj con)
    con))


(defn kill-all-connections []
  (doseq [con @all-connections]
    (.disconnect con))
  (reset! all-connections []))

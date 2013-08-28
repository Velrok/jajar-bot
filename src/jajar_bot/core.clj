(ns jajar-bot.core
  (:require [jajar-bot.xmpp :as xmpp]
            [guns.cli.optparse :refer [parse]]
            [clojure.string :as s]))


(defn shows [& args]
  :ok)


(defn movies [& args]
  :ok)


(def commands
  {"shows" shows
   "movies" movies})


(defn help [commands chosen]
  (str "Unknown command " chosen "\n"
       "Know commands:\n\n"
       (s/join "\n" (keys commands))))


(defn msg-handler [message]
  (let [args (s/split message #" ")
        main-command (first args)]
    (if (not (contains? (set (keys commands))
                        main-command))
      (help commands main-command))))

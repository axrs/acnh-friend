(ns io.axrs.acnh-friend.dom.event
  (:require
    [io.axrs.acnh-friend.dom.input :as input]))

(defn prevent-default [^js event]
  (some-> event (.preventDefault))
  event)

(defn stop-propagation [^js event]
  (some-> event (.stopPropagation))
  event)

(def stop-bubble (comp prevent-default stop-propagation))

(defn target [^js event]
  (some-> event (.-target)))

(def target-value (comp input/value target))

(ns io.axrs.acnh-friend.dom.input)

(defn value [^js input]
  (some-> input (.-value)))

(defn focus [^js input]
  (some-> input (.focus)))

(defn uncheck [^js input]
  (set! (.-checked input) false))

(defn check [^js input]
  (set! (.-checked input) true))

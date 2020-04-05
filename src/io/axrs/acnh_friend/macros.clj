(ns io.axrs.acnh-friend.macros)

(defmacro read-file [file]
  (->> file
       clojure.core/slurp
       read-string))

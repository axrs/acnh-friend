(defproject io.axrs/acnh-friend "0.0.1-SNAPSHOT"
  :description "Animal Crossing New Horizons Tool"
  :url "https://github.com/axrs/acnh-friend"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[thheller/shadow-cljs "2.8.94"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [org.clojure/core.async "1.1.587"]
                 [io.axrs/freactive "0.2.0-SNAPSHOT"]]
  :jvm-opts ["-Xmx1g"]
  :source-paths ["src"]
  :test-paths ["test"])

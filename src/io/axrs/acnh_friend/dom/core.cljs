(ns io.axrs.acnh-friend.dom.core
  (:refer-clojure :exclude [find])
  (:require [clojure.string :as str]))

(defn find [selector]
  (when-not (str/blank? selector)
    (.querySelector js/document selector)))

(defn first-child [^js node]
  (some-> node (.-firstChild)))

(defn last-child [^js node]
  (some-> node (.-lastChild)))

(defn remove-child [^js parent ^js child]
  (when (and parent child)
    (.removeChild parent child)))

(defn clear [^js node]
  (when (first-child node)
    (remove-child node (last-child node))
    (recur node)))


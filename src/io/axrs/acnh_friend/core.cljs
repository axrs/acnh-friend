(ns io.axrs.acnh-friend.core
  (:refer-clojure :exclude [atom])
  (:require-macros [io.axrs.acnh-friend.macros :refer [read-file]])
  (:require [freactive.core :refer [atom cursor lens-cursor rx]]
            [freactive.dom :as dom]
            [clojure.string :as str]))

(enable-console-print!)

(def fish (read-file "data/fish.edn"))
(def bugs (read-file "data/bugs.edn"))
(def fish-n-bugs (sort-by :name (concat bugs fish)))

(defonce state (atom {:critters fish-n-bugs}))

(defn render-critter [{:keys [name price location] :as critter}]
  [:tr
   [:td name]
   [:td location]
   [:td price]])

(defn render-critters [critters-cursor]
  (concat
    [:table
     [:thead
      [:tr
       [:th.name "Name"]
       [:th.location "Location"]
       [:th.price "Price"]]]]
    [[:tbody (rx (mapv render-critter @critters-cursor))]]))

(defn- set-search [^js event]
  (let [v (some-> event (.-target) (.-value))]
    (swap! state assoc :search v)))

(defn- filter-critters [state]
  (let [{:keys [search critters]} state]
    (if (str/blank? search)
      critters
      (let [search (str/lower-case search)]
        (filter (comp #(str/includes? % search) str/lower-case :name) critters)))))

(defn view []
  (let [search (cursor state :search)
        critters-cursor (lens-cursor state filter-critters)]
    [:div
     [:input {:placeholder "Search"
              :value       (rx @search)
              :on-keyup    set-search
              :on-change   set-search}]
     (render-critters critters-cursor)]))

(defn render []
  (dom/mount! (.getElementById js/document "root") (view)))

(render)

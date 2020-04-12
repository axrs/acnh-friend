(ns io.axrs.acnh-friend.operations
  (:refer-clojure :exclude [atom])
  (:require-macros
    [io.axrs.acnh-friend.macros :refer [read-file]])
  (:require
    [clojure.string :as str]
    [freactive.core :refer [atom]]))


(defonce fish (read-file "data/fish.edn"))
(defonce bugs (read-file "data/bugs.edn"))
(defonce months (->> ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"]
                     (map (juxt identity (comp keyword str/lower-case)))))
(defonce fish-n-bugs (sort-by :name (concat bugs fish)))

(defonce locations (->> (map :location fish-n-bugs)
                        set
                        sort))

(defonce state (atom {:locations  :all
                      :hemisphere :southern
                      :month      :all}))

(defn set-search [value]
  (swap! state assoc :search value))

(defn clear-search []
  (swap! state dissoc :search))

(defn set-hemisphere [hemisphere]
  (swap! state assoc :hemisphere hemisphere))

(def set-southern-hemisphere #(set-hemisphere :southern))
(def set-northern-hemisphere #(set-hemisphere :northern))

(defn set-month [month]
  (swap! state assoc :month month))

(defn toggle-location [location]
  (let [current-locations (:locations @state)]
    (cond
      (= :all location)
      (swap! state assoc :locations :all)

      (= :all current-locations)
      (swap! state assoc :locations #{location})

      (contains? current-locations location)
      (swap! state update :locations disj location)

      :else (swap! state update :locations conj location))))

(defn set-sort-by [col]
  (let [{:keys [sort-col sort-dir]} @state]
    (if (= sort-col col)
      (swap! state assoc
             :sort-dir (if (= > sort-dir) < >))
      (swap! state assoc
             :sort-col col
             :sort-dir <))))

(defn- filter-critters [state]
  (let [{:keys [search hemisphere month locations sort-col sort-dir]} state]
    (cond->> fish-n-bugs
      (not (= :all month))
      (filter (fn [{:as critter}]
                (contains? (get-in critter [:months hemisphere]) month)))

      (not (= :all locations))
      (filter (comp (partial contains? locations) :location))

      (not (str/blank? search))
      (filter (comp #(str/includes? % (str/lower-case search)) str/lower-case :name))

      sort-col
      (sort-by sort-col (or sort-dir <)))))


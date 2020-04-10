(ns io.axrs.acnh-friend.core
  (:refer-clojure :exclude [atom])
  (:require-macros [io.axrs.acnh-friend.macros :refer [read-file]])
  (:require [freactive.core :refer [atom cursor lens-cursor rx]]
            [freactive.dom :as dom]
            [clojure.string :as str]))

(enable-console-print!)

(def fish (read-file "data/fish.edn"))
(def bugs (read-file "data/bugs.edn"))
(def months (->> ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"]
                 (map (juxt identity (comp keyword str/lower-case)))))
(def fish-n-bugs (sort-by :name (concat bugs fish)))

(defonce state (atom {:critters   fish-n-bugs
                      :hemisphere :southern
                      :month      :all}))

(defn render-critter [{:keys [name price location time] :as critter}]
  [:tr
   [:td name]
   [:td location]
   [:td time]
   [:td price]])

(defn render-critters [critters-cursor]
  (concat
    [:table
     [:thead
      [:tr
       [:th.name "Name"]
       [:th.location "Location"]
       [:th.time "Time"]
       [:th.price "Price"]]]]
    [[:tbody (rx (mapv render-critter @critters-cursor))]]))

(defn- set-search [^js event]
  (let [v (some-> event (.-target) (.-value))]
    (swap! state assoc :search v)))

(defn- clear-search []
  (swap! state dissoc :search))

(defn- stop-bubble [^js event]
  (doto event
    (.stopPropagation)
    (.preventDefault)))

(defn- toggle-hemisphere [key ^js event]
  (stop-bubble event)
  (js/console.log "Toggle Hemisphere" (name key))
  (swap! state assoc :hemisphere key))

(defn- toggle-month [key ^js event]
  (stop-bubble event)
  (js/console.log "Toggle Month" (name key))
  (swap! state assoc :month key))

(defn- filter-critters [state]
  (let [{:keys [search critters hemisphere month]} state]
    (cond->> critters
      (not (= :all month))
      (filter (fn [{:as critter}]
                (contains? (get-in critter [:months hemisphere]) month)))
      (not (str/blank? search))
      (filter (comp #(str/includes? % (str/lower-case search)) str/lower-case :name)))))

(defn tick [key current-val]
  (when (= key current-val)
    [:span "✔"]))

(defn nav [hemisphere-cursor month-cursor search-cursor]
  [:nav {:fx true}

   [:label [:input {:type "checkbox"}]
    [:header [:a "ACNH - Friend"]]
    [:ul
     [:li [:a "Hemisphere"]
      (rx
        (let [selected-hemisphere @hemisphere-cursor]
          [:menu
           [:menuitem {:on-click (partial toggle-hemisphere :northern)}
            [:a "Northern"
             (tick :northern selected-hemisphere)]]
           [:menuitem {:on-click (partial toggle-hemisphere :southern)}
            [:a "Southern"
             (tick :southern selected-hemisphere)]]]))]
     [:li [:a "Months"]
      (rx
        (let [selected-month @month-cursor]
          [:menu
           [:menuitem {:on-click (partial toggle-month :all)}
            [:a "All"
             (tick :all selected-month)]]
           (for [[label key] months]
             [:menuitem {:on-click (partial toggle-month key)}
              [:a label
               (tick key selected-month)]])]))]]]
   [:label#search
    [:input {:placeholder "Search"
             :value       (rx @search-cursor)
             :on-keyup    set-search
             :on-change   set-search}]
    [:button {:on-click clear-search} "Clear"]]])

(defn root []
  (let [search-cursor (cursor state :search)
        hemisphere-cursor (cursor state :hemisphere)
        month-cursor (cursor state :month)
        critters-cursor (lens-cursor state filter-critters)]
    [
     (nav hemisphere-cursor month-cursor search-cursor)
     [:section
      ;[:label#search
      ; [:input {:placeholder "Search"
      ;          :value       (rx @search)
      ;          :on-keyup    set-search
      ;          :on-change   set-search}]
      ; [:button {:on-click clear-search} "Clear"]]
      (render-critters critters-cursor)]]))

(defn render []
  (let [root-node (.getElementById js/document "root")]
    (set! (.-innerHTML root-node) "")
    (dom/mount! root-node (root))))

(render)

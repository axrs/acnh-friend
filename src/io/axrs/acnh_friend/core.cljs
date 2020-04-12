(ns io.axrs.acnh-friend.core
  (:refer-clojure :exclude [atom])
  (:require-macros [io.axrs.acnh-friend.macros :refer [read-file]])
  (:require
    [clojure.string :as str]
    [freactive.core :refer [atom cursor lens-cursor rx]]
    [freactive.dom :as frx-dom]
    [io.axrs.acnh-friend.dom.core :as dom]
    [io.axrs.acnh-friend.dom.input :as input]
    [io.axrs.acnh-friend.dom.event :as event]))

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
  (->> event
       event/target-value
       (swap! state assoc :search)))

(defn- close-menu []
  (->> "#menu-toggle"
       dom/find
       input/uncheck))

(defn- focus-search []
  (->> "#search input"
       dom/find
       input/focus))

(defn- clear-search []
  (close-menu)
  (swap! state dissoc :search)
  (focus-search))

(defn- toggle-hemisphere [key ^js _]
  (swap! state assoc :hemisphere key))

(defn- toggle-month [key ^js _]
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
   [:label {:on-focus #(prn "Menu Focused")
            :on-blur  #(prn "Menu Blur")}
    [:input#menu-toggle {:type "checkbox"}]
    [:header [:a "ACNH - Friend"]]
    [:ul
     [:li [:a "Hemisphere"]
      (rx
        (let [selected-hemisphere @hemisphere-cursor]
          [:menu
           [:menuitem {:on-click (comp (partial toggle-hemisphere :northern) event/stop-bubble)}
            [:a "Northern"
             (tick :northern selected-hemisphere)]]
           [:menuitem {:on-click (comp (partial toggle-hemisphere :southern) event/stop-bubble)}
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
             :on-focus    close-menu
             :on-keyup    set-search
             :on-change   set-search}]
    [:button {:on-click clear-search} "Clear"]]])

(defn root []
  (let [search-cursor (cursor state :search)
        hemisphere-cursor (cursor state :hemisphere)
        month-cursor (cursor state :month)
        critters-cursor (lens-cursor state filter-critters)]
    [(nav hemisphere-cursor month-cursor search-cursor)
     [:section
      (render-critters critters-cursor)]]))

(defn render []
  (when-let [root-node (dom/find "#root")]
    (dom/clear root-node)
    (frx-dom/mount! root-node (root)))
  nil)

(defonce render-once (delay (render)))
@render-once

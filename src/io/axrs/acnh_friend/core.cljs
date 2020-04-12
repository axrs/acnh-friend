(ns io.axrs.acnh-friend.core
  (:require-macros [io.axrs.acnh-friend.macros :refer [read-file]])
  (:require
    [freactive.core :refer [cursor lens-cursor rx]]
    [freactive.dom :as frx-dom]
    [io.axrs.acnh-friend.dom.core :as dom]
    [io.axrs.acnh-friend.dom.event :as event]
    [io.axrs.acnh-friend.dom.input :as input]
    [io.axrs.acnh-friend.operations :as ops]))

(enable-console-print!)

(defn- close-menu []
  (->> "#menu-toggle"
       dom/find
       input/uncheck))

(defn- focus-search []
  (->> "#search input"
       dom/find
       input/focus))

(defn- clear-search []
  (ops/clear-search)
  (close-menu)
  (focus-search))

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
       [:th.name {:on-click #(ops/set-sort-by :name)} "Name"]
       [:th.location {:on-click #(ops/set-sort-by :location)} "Location"]
       [:th.time {:on-click #(ops/set-sort-by :time)} "Time"]
       [:th.price {:on-click #(ops/set-sort-by :price)} "Price"]]]]
    [[:tbody (rx (mapv render-critter @critters-cursor))]]))

(defn tick [key current-val]
  (when (= key current-val)
    [:span "✔"]))

(defn nav [hemisphere-cursor month-cursor search-cursor location-cursor]
  [:nav {:fx true}
   [:label
    [:input#menu-toggle {:type "checkbox"}]
    [:header [:a "ACNH - Friend"]]
    [:ul

     [:li [:a "Hemisphere"]
      (rx
        (let [selected-hemisphere @hemisphere-cursor]
          [:menu
           [:menuitem {:on-click (comp ops/set-northern-hemisphere event/stop-bubble)}
            [:a "Northern"
             (tick :northern selected-hemisphere)]]
           [:menuitem {:on-click (comp ops/set-southern-hemisphere event/stop-bubble)}
            [:a "Southern"
             (tick :southern selected-hemisphere)]]]))]

     [:li [:a "Months"]
      (rx
        (let [selected-month @month-cursor]
          [:menu
           [:menuitem {:on-click (comp #(ops/set-month :all) event/stop-bubble)}
            [:a "All"
             (tick :all selected-month)]]
           (for [[label key] ops/months]
             [:menuitem {:on-click (comp #(ops/set-month key) event/stop-bubble)}
              [:a label
               (tick key selected-month)]])]))]

     [:li [:a "Locations"]
      (rx
        (let [locations @location-cursor]
          [:menu
           [:menuitem {:on-click (comp #(ops/toggle-location :all) event/stop-bubble)}
            [:a "All"
             (tick :all locations)]]
           (for [location ops/locations]
             [:menuitem {:on-click (comp #(ops/toggle-location location) event/stop-bubble)}
              [:a location
               (when (contains? locations location)
                 [:span "✔"])]])]))]]]

   [:label#search
    (let [set-search (comp ops/set-search event/target-value)]
      [:input {:placeholder "Search"
               :value       (rx @search-cursor)
               :on-focus    close-menu
               :on-keyup    set-search
               :on-change   set-search}])
    [:button {:on-click clear-search} "Clear"]]])

(defn root []
  (let [search-cursor (cursor ops/state :search)
        hemisphere-cursor (cursor ops/state :hemisphere)
        location-cursor (cursor ops/state :locations)
        month-cursor (cursor ops/state :month)
        critters-cursor (lens-cursor ops/state ops/filter-critters)]
    [(nav hemisphere-cursor month-cursor search-cursor location-cursor)
     [:section
      (render-critters critters-cursor)]]))

(defn render []
  (when-let [root-node (dom/find "#root")]
    (dom/clear root-node)
    (frx-dom/mount! root-node (root)))
  nil)

(defonce render-once (delay (render)))
@render-once

#!/usr/bin/env cljog

(deps '[[crouton "0.1.2"]
        [com.rpl/specter "1.1.3"]])
(require '[crouton.html :as html])
(require '[clojure.pprint :refer [pprint]])
(require '[clojure.string :as str])
(use 'com.rpl.specter)

(def northern_html (slurp "/Users/axrs/Downloads/bugs_northern.html"))
(def southern_html (slurp "/Users/axrs/Downloads/bugs_southern.html"))

(defn- keep-vals [content]
  (filter (fn [x]
            (or (map? x)
                (not (str/blank? x))))
          content))

(defn ->critter [name img price location time jan feb mar apr may jun jul aug sep oct nov dec]
  (let [name (-> name first :content first str/trim)]
    {:name     name
     :price    (-> price first str/trim read-string)
     :location (-> location first str/trim)
     :time     (-> time first :content first str/trim)
     :months   (->> [jan feb mar apr may jun jul aug sep oct nov dec]
                    (map first)
                    (zipmap [:january :february :march :april :may :june :july :august :september :october :november :december])
                    (filter (comp (partial not= "-") str/trim second))
                    (map first)
                    set)}))

(defn- row->critter [month-key tr-map]
  (let [{:keys [months] :as critter} (->> tr-map
                                          :content
                                          (select (walker (fn [v] (and (map? v)
                                                                       (string? (get-in v [:content 0]))))))
                                          (map (comp flatten keep-vals :content))
                                          (apply ->critter))]
    (-> critter
        (dissoc :months)
        (assoc-in [:months month-key] months))))

(defn parse-critters [month-key html-file]
  (->> html-file
       html/parse-string
       (select (walker (fn [v] (and (map? v)
                                    (= :tr (:tag v))))))
       rest                                                 ; Drop heading
       (map (partial row->critter month-key))))

(defn merge-hemispheres [northern southern]
  (reduce
    (fn [col {:keys [name] :as entry}]
      (let [months (get-in entry [:months :southern])]
        (setval [ALL (comp (partial = name) :name) :months :southern] months col)))
    northern
    southern))

(let [northern-critters (parse-critters :northern northern_html)
      southern-critters (parse-critters :southern southern_html)]
  (->> (merge-hemispheres northern-critters southern-critters)
       vec
       pprint
       with-out-str
       (spit "../data/bugs.edn")))

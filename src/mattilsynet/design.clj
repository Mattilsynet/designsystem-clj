(ns mattilsynet.design
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def class-idx (read-string (slurp (io/resource "mattilsynet-design/css-modules.edn"))))

(defmacro ^:export classes [& classes]
  (->> classes
       (mapcat
        (fn [class]
          (get class-idx class [class])))
       (cons 'list)))

(defn ^:export load-svg
  "Loads an SVG from the design system"
  [path]
  (slurp (io/resource (str "public/mtds/" path ".svg"))))

(def base-path "mattilsynet-design")

(defn get-svg-path [id]
  (case (namespace id)
    "icon"
    (str base-path "/icons/" (name id) ".edn")

    "illustration"
    (str base-path "/illustrations/"
         (str/replace (name id) #"\." "/") ".edn")

    (throw (ex-info "Unknown svg kind, use either :icon/* or :illustration/*" {:id id}))))

(defn load-svg-resource [id]
  (-> (get-svg-path id)
      io/resource
      slurp
      read-string))

(defn ^:export render-svg [id & [attrs]]
  (if-let [svg (load-svg-resource id)]
    (update svg 1 merge attrs)
    (throw (ex-info (str "SVG " id " does not exist") {:id id}))))

(defn load-svg! [_id _resource])

(defmacro ^:export svg [id]
  (if (:ns &env)
    ;; ClojureScript må laste svg-en inn i bygget
    `(do
       (mattilsynet.design/load-svg! ~id ~(load-svg-resource id))
       ~id)
    ;; Clojure laster svg-en dynamisk ved bruk
    id))

(defn get-ids [path ns]
  (let [all-your-base (str base-path "/" path)]
    (->> (io/resource all-your-base)
         io/file
         file-seq
         (map str)
         (filter #(re-find #"\.edn$" %))
         (map #(second (str/split % (re-pattern (str all-your-base "/")))))
         (map #(keyword ns (-> %
                               (str/replace #"\.edn$" "")
                               (str/replace #"/" "."))))
         sort
         vec)))

(defmacro ^:export get-icon-ids []
  (get-ids "icons" "icon"))

(defmacro ^:export get-illustration-ids []
  (get-ids "illustrations" "illustration"))

(get-icon-ids)
[:icon/cow :icon/fish :icon/food :icon/plant :icon/water]

(get-illustration-ids)
[:illustration/animals.bird :illustration/animals.pig :illustration/aqua.fish :illustration/aqua.flounder :illustration/aqua.mussel-1 :illustration/aqua.mussel-2 :illustration/aqua.mussel-3 :illustration/aqua.mussels :illustration/aqua.person-fish :illustration/aqua.person-fish-seaweed :illustration/aqua.person-mussels :illustration/aqua.seaweed :illustration/aqua.shrimp :illustration/aqua.water-plant :illustration/cosmetics.flask-perfume :illustration/cosmetics.flask-pipette :illustration/cosmetics.flask-pump :illustration/cosmetics.flasks :illustration/cosmetics.jar :illustration/cosmetics.person-flasks :illustration/cosmetics.person-tubes :illustration/cosmetics.tube-1 :illustration/cosmetics.tube-2 :illustration/food.apple :illustration/food.apple-cup :illustration/food.banana :illustration/food.banana-apple-cup :illustration/food.farmer-turnip-plant :illustration/food.pear :illustration/food.person-fork-bush :illustration/food.person-pear :illustration/food.turnip :illustration/humans.farmer-inviting :illustration/humans.farmer-pig-plant :illustration/humans.farmer-pitchfork :illustration/humans.inspector :illustration/humans.meeting-plant :illustration/humans.person-apron :illustration/humans.person-apron-bush :illustration/humans.person-arm :illustration/humans.person-flower :illustration/humans.team :illustration/plants.flower :illustration/plants.flower-seeds :illustration/plants.grass-bush :illustration/plants.oak-leaf :illustration/plants.plant :illustration/plants.plant-round-leaves :illustration/plants.seaweed :illustration/vision.person-animal-nature-1 :illustration/vision.person-animal-nature-2 :illustration/water.drop :illustration/water.faucet :illustration/water.person-drop :illustration/water.person-faucet-drop]

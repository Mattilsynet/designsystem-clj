(ns mattilsynet.build
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [hickory.core :as hiccup])
  (:import (java.io File)))

(def resource-dir "resources/public/mtds")

(def package-json (json/read-str (slurp (io/resource "package.json"))))

(def exports
  (let [strip-fs-extras #(-> %
                             (str/replace #"^\./" "")
                             (str/replace #"/\*" ""))]
    (-> package-json
        (get "exports")
        (update-keys strip-fs-extras)
        (update-vals strip-fs-extras))))

(defn ensure-dir! [^String dir]
  (.mkdirs (io/file dir)))

(defn ensure-parent-dir! [^String path]
  (ensure-dir! (.getParent (io/file path))))

(defn load-css-modules [json-str]
  (->> (json/read-str json-str)
       (map
        (fn [[k classes]]
          [(keyword k) (str/split classes #" ")]))
       (into (sorted-map ))))

(defn export-css-modules [file class->classes]
  (ensure-parent-dir! file)
  (spit file (with-out-str
               (pprint/pprint class->classes))))

(defn build-css-modules [& _]
  (->> (io/resource (get exports "styles.json"))
       slurp
       load-css-modules
       (export-css-modules "resources/mattilsynet-design/css-modules.edn")))

(defn to-hiccup [markup]
  (-> markup
      hiccup/parse
      hiccup/as-hiccup
      first
      last
      last
      (update-in [1] #(-> % (dissoc :viewbox) (assoc :viewBox (:viewbox %))))))

(defn export-svg [^File file]
  (let [path (second (str/split (.getPath file) #"mtds/"))
        target (str "resources/mattilsynet-design/"
                    (str/replace path #"\.svg$" ".edn"))]
    (ensure-parent-dir! target)
    (spit target (to-hiccup (slurp file)))))

(defn export-svgs [resource]
  (->> (file-seq (io/file resource))
       (filter #(str/ends-with? (.getPath ^File %) ".svg"))
       (run! export-svg)))

(defn export-all-svgs [& _]
  (export-svgs (io/resource (get exports "logo")))
  (export-svgs (io/resource (get exports "icons")))
  (export-svgs (io/resource (get exports "illustrations"))))

(defn load-committed-pom []
  (:out (shell/sh "git" "cat-file" "-p" "HEAD:pom.xml")))

(defn get-version [pom]
  (second (re-find #"<version>([\d\.]+)</version>" pom)))

(defn get-release-number [pom]
  (parse-long (last (str/split (get-version pom) #"\."))))

(defn bump-version [& _]
  (let [pom (load-committed-pom)
        release-num (get-release-number pom)
        mtds-version (get package-json "version")
        next (str mtds-version "." (inc release-num))]
    (spit "pom.xml" (str/replace pom (str mtds-version "." release-num) next))
    (println next)))

(ns crawler.core
  (:require [crawler.crawl :as crawl]
            [crawler.render :as render]
            [clojure.java.io :as io]))

(defn parse-int [s]
  (Integer. (re-find #"\d+" s)))

(defn mkdir [dir-name]
  (or (.mkdir (java.io.File. dir-name)) nil))

(defn ls [dir-name]
  (file-seq (io/file dir-name)))

(def site-path "public/")
;(def site-path "private/")

(defn last-event-page [list-key]
  (->> (ls (str site-path (name list-key) "/"))
       (filter #(.isFile %))
       (map #(.getName %))
       (apply max-key parse-int)))

(def games (crawl/games))
(defn generate-pages []
  (doseq [[list-key event-map] games]
    (mkdir (str site-path (name list-key)))
    (let [events (crawl/events event-map)]
      (doseq [[event-id games-map] event-map]
        (spit (str site-path (name list-key) "/" (get events event-id) ".html")
              (render/event-page games-map events list-key event-id)))))
  (spit (str site-path "index.html")
        (render/backlog-page
         games
         (reduce #(assoc %1 %2 (last-event-page %2)) {} (keys games)))))

(generate-pages)

(ns crawler.render
  (:require [clojure.string :refer [split capitalize join]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]))

(defn header
  ([title] (header title ""))
  ([title path-to-root]
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:link {:href (str path-to-root "css/main.css"):rel "stylesheet"}]
    [:script {:src (str path-to-root "js/main.js")}]
    [:title title]]))

(defn page
  ([title content] (page title "" content))
  ([title path-to-root content]
   (html5 (header title path-to-root)
          [:body {:onload "init()"}
           content])))

(defn link-name [list-key]
  (->> (-> list-key
           name
           (split #"-"))
       (map capitalize)
       (join " ")))

(defn backlog-page [games-map last-event-pages]
  (page "Ludum Dare Backlog"
        (list
          [:h1 "Ludum Dare Backlog"]
          [:ul#table
           ;; TODO set to the last event
           (map #(vector :li [:a {:href (str (name %) "/" (last-event-pages %))}
                              (link-name %)])
                (keys games-map))])))

(defn events-header [events]
  [:ul#events
   (map #(vector :li [:a {:href (str % ".html")} %])
        (vals (into (sorted-map-by >) events)))])

(defn event-page [games events list-key event-id]
  (page (str (link-name list-key) " — Ludum Dare Backlog") "../"
        (list
          (events-header events)
          [:h1 (str "Ludum Dare " (get events event-id))]
          [:ul#table
           (map #(vector :li
                         [:b (:author %)]
                         " made "
                         [:a {:href (str "https://ldjam.com" (:path %))} (:name %)])
                games)])))

(ns crawler.render
  (:require [clojure.string :refer [split capitalize join]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5]]))

(defn header [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:href "../css/main.css" :rel "stylesheet"}]
   [:script {:src "../js/main.js"}]
   [:title title]])

(defn page [title content]
  (html5 (header title)
         [:body {:onload "init()"}
          content]))

(defn link-name [list-key]
  (->> (-> list-key
           name
           (split #"-"))
       (map capitalize)
       (join " ")))

(link-name :pixel-prophecy)

(defn backlog-page [games-map]
  (page "Ludum Dare Backlog"
        [:<>
         [:h1 "Ludum Dare Backlog"]
         [:ul#table
          ;; TODO set to the last event
          (map #(vector :li [:a {:href (name %)} (link-name %)])
               (keys games-map))]]))

(comment
  (println (page "Foo" [:span.foo "bar"])))

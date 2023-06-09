(ns crawler.crawl
  (:require [crawler.data :as data]
            [clojure.string :refer [join]]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(defn crawl [path]
  (println "Crawling" path)
  (let [res (http/get (str "https://api.ldjam.com/vx" path))]
    ; TODO handle errors
    (json/parse-string (:body res) true)))

(defn crawl-nodes [ids]
  (crawl (str "/node2/get/" (join "+" ids))))

(defn crawl-user-id [author-name]
  (let [res (crawl (str "/node2/walk/1/users/" author-name))]
    (cond
      (not= (:status res) 200) nil
      ;; users are stored in the "users" node, therefore path to user consists
      ;; of 2 entries. (count path) = 1 means, there's no user with the
      ;; specified name
      (= (count (:path res)) 1) nil
      :else (:node_id res))))

(defn crawl-game-ids [user-id]
  (let [res (crawl (str "/node/feed/" user-id "/authors/item/game?limit=250"))]
    (if (= (:status res) 200)
      (mapv :id (:feed res))
      nil)))

(defn games []
  (reduce
   (fn [acc [list-name authors]]
     (assoc acc
            list-name
            (apply merge-with #(if (vector? %1) (conj %1 %2) [%1 %2])
                   (reduce
                    (fn [acc author]
                      (conj acc
                            (->> author
                                 crawl-user-id
                                 crawl-game-ids
                                 crawl-nodes
                                 :node
                                 (map #(vector (:parent %)
                                               (assoc
                                                ;; TODO remove :parent if not needed
                                                (select-keys % [:name :path :parent])
                                                :author author)))
                                 (into {}))))
                    [] authors))))
   {} data/lists))

;; actually, it's (nth games-map)
(defn events [games-map]
  (->> games-map
       keys
       (into #{})
       crawl-nodes
       :node
       (reduce #(assoc %1 (:id %2) (:slug %2)) {})))

(ns rssminer.handlers.feedreader
  (:use [rssminer.util :only [session-get to-int]]
        [rssminer.search :only [search*]]
        [rssminer.db.feed :only [fetch-latest-feed fetch-unread-meta
                                 fetch-unread-count-by-tag]]
        [rssminer.db.subscription :only [fetch-subscriptions-by-user]])
  (:require [rssminer.views.feedreader :as view]
            [rssminer.config :as cfg]))

(defn landing-page [req]
  (view/landing-page))

(defn- compute-by-time [unread]
  (let [[day week month] (rssminer.time/time-pairs)
        k-day (str "d_" day)
        k-week (str "w_" week)
        k-month (str "m_" month)]
    (persistent! (reduce (fn [m {:keys [published_ts] :as i}]
                           (let [key (cond (> published_ts day) k-day
                                           (> published_ts week) k-week
                                           (> published_ts month) k-month
                                           :else "older")]
                             (assoc! m key (inc (get m key 0)))))
                         (transient {}) unread))))

(defn compute-by-sub [unread]
  (persistent! (reduce (fn [m {:keys [rss_link_id] :as i}]
                         (assoc! m rss_link_id
                                 (inc (get m rss_link_id 0))))
                       (transient {}) unread)))

(defn index-page [req]
  (let [user-id (:id (session-get req :user))
        unread (fetch-unread-meta user-id)]
    (view/index-page {:by_sub (compute-by-sub unread)
                      :by_time (compute-by-time unread)
                      :by_tag (fetch-unread-count-by-tag
                               (map :f_id unread))
                      :subscriptions (fetch-subscriptions-by-user
                                      user-id)})))

(defn dashboard-page [req]
  (view/dashboard-page))

(defn search [req]
  (let [{:keys [term limit] :or {limit 20}} (:params req)]
    (search* term (to-int limit) :user-id (:id (session-get req :user)))))

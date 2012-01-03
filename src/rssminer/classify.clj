(ns rssminer.classify
  (:use [clojure.tools.logging :only [info]]
        [rssminer.search :only [searcher]])
  (:require [rssminer.db.user-feed :as uf])
  (:import rssminer.classfier.NaiveBayes))

(defn re-build-model [user-id]
  (let [ups (uf/fetch-up-ids user-id)
        downs (uf/fetch-down-ids user-id)]
    (if (and (> (count ups) 0)
             (> (count downs) 0))
      (let [model (NaiveBayes/train @searcher ups downs)]
        (info "re-build" user-id "'s model; up"
              (count ups) "; down" (count downs))
        model)                          ; return model
      (info "not re-build" user-id "'s model;"))))

(defn re-compute-sysvote [user-id since-time]
  (if-let [model (re-build-model user-id)]
    (let [ids (uf/fetch-unvoted-feedids user-id since-time)
          votes (NaiveBayes/classify @searcher model ids)]
      (dorun (map (fn [id score]
                    (uf/insert-sys-vote user-id id score)) ids votes))
      votes)))

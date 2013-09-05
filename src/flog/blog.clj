(ns flog.blog
  (:use net.cgrand.enlive-html
        markdown.core
        flog.html
        [com.ashafa.clutch :exclude [assoc! dissoc! conj!]]))

(def blog-form (html-snippet (slurp "/home/ethan/projects/flog/src/templates/blog.html")))

(def blog-admin-page (at private-templt [:#content] (append blog-form)))

(def ^:private db (get-database "blog-dev"))

(defn define-views []
  (with-db db
        (save-view "blog-posts" 
          (view-server-fns :clojure
            {:by-timestamp {:map (fn [doc] (when (and (:md doc)
                                       (:title doc)
                                       (:timestamp doc))
                             [[(:timestamp doc)
                               doc]]))}}))))
;; (defn define-views []
;;   (with-db db 
;;     (save-view "blog-posts"
;;                (view-server-fns :cljs
;;                                 {:by-timestamp {:map (fn [doc]
;;                                                        (js/emit (when 
;;                                                                   (and (:md doc)
;;                                                                        (:title doc)
;;                                                                        (:timestamp doc))
;;                                                                     doc) nil))}}))))

(defn post-blog [title md timestamp]
  (with-db db 
    (put-document {:title title
                   :md md
                   :html ""
                   :pdf ""
                   :timestamp timestamp})))


(defn update-blog [db id])

(defn delete-blog [db id])


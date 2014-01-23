(ns flog.blog
  (:use net.cgrand.enlive-html
        markdown.core
        flog.html
        [com.ashafa.clutch :exclude [assoc! dissoc! conj!]]))

(def blog-snips (html-snippet (slurp "src/templates/blog.html")))
(def blog-form (select blog-snips [:#blog-form]))
(def blog-admin-page (at private-templt [:#content] (append blog-form)))

(def ^:private db (get-database "blog-dev"))

(defn define-views []
  (with-db db 
    (save-view "blog-posts"
               (view-server-fns :cljs
                                {:by-timestamp 
                                 {:map (fn [doc] 
                                         (js/emit (aget doc "timestamp") doc nil))}}))))

(defn post-blog [title md]
  (with-db db 
    (put-document {:title title
                   :md md
                   :html (md-to-html-string md) 
                   :pdf ""
                   :timestamp (System/currentTimeMillis)})))

(defn get-posts []
  (with-db db 
    (get-view "blog-posts" "by-timestamp" {:descending true})))

(defn blog-posts []
  (for [post (get-posts)]
    (let [pst (:value post)]
      (tag :content (list  
           (tag :tag "p" :content (:title pst))
           (html-snippet (:html pst)))))))

(defn update-blog [id])

(defn delete-blog [id])

(defn blog-page [auth] 
  (at (if auth private-templt templt) [:#content] (append (blog-posts))))

(ns flog.blog
  (:use net.cgrand.enlive-html
        markdown.core
        flog.html
        [com.ashafa.clutch :exclude [assoc! dissoc! conj!]]))

(def blog-snips (html-snippet (slurp "src/templates/blog.html")))
(def blog-form (select blog-snips [:#blog-form]))
(def blog-admin-page (at blog-private-templt [:#content] (append blog-form)))

(def ^:private db (get-database "blog-dev"))

(defn create-views []
  (with-db db 
    (save-view "blog-posts"
               (view-server-fns :cljs 
                                 {:by-timestamp {:map (fn [doc] (js/emit (aget doc "timestamp") doc nil))}
                                  :unpublished {:map (fn [doc] 
                                                       (if (aget doc "private") 
                                                         (js/emit (aget doc "timestamp") doc nil)))}
                                  :published {:map (fn [doc] 
                                                       (if (false? (aget doc "private")) 
                                                         (js/emit (aget doc "timestamp") doc nil)))}}))))

(defn post-blog [title md]
  (with-db db
    (put-document {:title title
                   :md md
                   :html (md-to-html-string md) 
                   :pdf ""
                   :private true
                   :timestamp (System/currentTimeMillis)})))

(defn get-posts [level]
  (with-db db 
    (get-view "blog-posts" level {:descending true})))

(defn blog-posts [level]
  (for [post (get-posts level)]
    (let [pst (:value post)]
      (tag :content (list  
           (tag :tag "h3" :content (:title pst))
           (tag :tag "h6" :content (:timestamp pst))
           (html-snippet (:html pst)))))))

(defn update-blog [id])

(defn delete-blog [id])

(defn blog-page [auth] 
  ;; (at (if auth blog-private-templt blog-templt) [:#content] (append (blog-posts))))
  (if auth (at blog-private-templt [:#content] (append (blog-posts "by-timestamp")))
           (at blog-templt [:#content] (append (blog-posts "published")))))


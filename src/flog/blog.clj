(ns flog.blog
  (:use net.cgrand.enlive-html
        markdown.core
        flog.html
        [com.ashafa.clutch :exclude [assoc! dissoc! conj!]]))

(def blog-form (html-snippet (slurp "src/templates/blog.html")))

(def blog-admin-page (at private-templt [:#content] (append blog-form)))

(def ^:private db (get-database "blog-dev"))
; (def db (get-database "blog-dev"))

(defn define-views []
  (with-db db 
    (save-view "blog-posts"
               (view-server-fns {:language :cljs
                                 :optimizations :advanced
                                 :pretty-print false 
                                 :main 'couchview/main}
                                {:by-timestamp {:map [(ns couchview)
                                                      (defn view 
                                                        [title md]
                                                        (str title "," md))
                                                      (defn ^:export main
                                                        [doc]
                                                        (js/emit (aget doc "timestamp") 
                                                                 ; (view (aget doc "title") (aget doc "md")) nil))]}}))))
                                                                 doc nil))]}}))))

(defn post-blog [title md timestamp]
  (with-db db 
    (put-document {:title title
                   :md md
                   :html ""
                   :pdf ""
                   :timestamp timestamp})))


(defn update-blog [db id])

(defn delete-blog [db id])

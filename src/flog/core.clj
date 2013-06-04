(ns flog.core
  (:use compojure.core
        ring.adapter.jetty
        net.cgrand.enlive-html)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

;; TODO replace this with edn
(def user-file "src/flog/user.clj")
(def users (ref (read-string (slurp user-file))))

(def map-file "src/flog/user-map.clj")
(def user-map (ref (read-string (slurp map-file))))

(derive ::admin ::user)

(defn lookup [usr] (spit "users.out" @users) (@users usr))

;; ======================
;; templates for enlive
;; ======================
(def index-tmpl (html-resource "templates/index.html"))
(def login-tmpl (html-resource "templates/index.html"))
(def snips-tmpl (html-resource "templates/snippets.html"))

(def r '({:tag :tr, :attrs nil, :content nil}))
(def c '({:tag :td, :attrs nil, :content nil}))
(def rc '({:tag :tr, :attrs nil, :content ({:tag :td, :attrs nil, :content nil})}))
(def t '({:tag :table :attrs nil :content nil}))
;; ======================
;; page renderers
;; ======================
(defn index [acctid]
  (emit* 
    (at index-tmpl
        [:something])))

(defn admin [acctid]
  (emit* 
    (at index-tmpl
        [:something])))

(defroutes main-routes
  ;; ======================
  ;; authenticated routes
  ;; ======================
  (context "/admin" request
           (friend/wrap-authorize
             (routes
               (GET "/" [] (let [acctid (name ((keyword
                                                 (str (:identity (friend/current-authentication))))
                                                 @user-map))]
                             (apply str (index acctid))))
               (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
               )#{::user}))
  ;; ======================
  ;; unauthenticated routes
  ;; ======================
  (GET "/" [] (apply str (emit* index-tmpl)))
  (GET "/login" [] (apply str (emit* login-tmpl)))
  (route/files "")
  (route/not-found "404 baby"))

(def site (handler/site
           (friend/authenticate main-routes
                                {:credential-fn (partial creds/bcrypt-credential-fn lookup)
                                 :workflows [(workflows/interactive-form)]})))

(defn flog [routedef]
  (run-jetty routedef {:port 8080}))


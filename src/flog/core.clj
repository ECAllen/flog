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

;; HTML elements
(def a '({:tag :a :attrs nil :content nil}))
(def script '({:tag :script :attrs nil :content nil}))
(def link '({:tag :link :attrs nil :content nil}))

;; table defs
;; t = table, r = row, c = column
(def r '({:tag :tr, :attrs nil, :content nil}))
(def c '({:tag :td, :attrs nil, :content nil}))
(def rc (transform r [:tr] (content c)))
(def t '({:tag :table :attrs nil :content 
          ({:tag :tr, :attrs nil, :content 
            ({:tag :td, :attrs nil, :content nil})})}))

;; divs
(def div '({:tag :div :attrs nil :content nil}))
(def div-r '({:tag :div :attrs {:class "row"} :content nil}))
(def div-c '({:tag :div :attrs {:class "span6"} :content nil}))
(defn div-c [width & attrs] 
  (let [spn (str "span" width)]
    (list {:tag :div :attrs {:class spn} :content nil})))
(def div-body (transform div [:div] (set-attr :id "body")))

;; bootstrap 
(def div-container (transform div [:div] (set-attr :class "container-fluid" :id "container")))
(def div-navbar (transform div [:div] (set-attr :id "nav" :class "navbar navbar-inverse")))
(def div-navbar-inner (at div [:div] 
                          (set-attr :class "navbar-inner")
                          [:div]
                          (content (at a [:a] (set-attr :class "brand" :href "#")
                                         [:a] (content "Flog"))
                                   (at a [:a] (set-attr :class "btn btn-inverse pull-right" :href "/main")
                                         [:a] (content "Sign in")))))

(def link-boot (transform link [:link] 
                          (set-attr :href "bootstrap/css/bootstrap.min.css" 
                                    :rel "stylesheet" 
                                    :media "screen")))

(def script-jquery (transform script [:script] (set-attr :src "http://code.jquery.com/jquery.js")))
(def script-boot (transform script [:script] (set-attr :src "bootstrap/js/bootstrap.min.js")))
(def posts (div-c 12))

;; ======================
;; page renderers
;; ======================
(defn index []
  (emit* 
    (at index-tmpl
        [:head]
        (append link-boot) 

        [:body]
        (append (at div-container 
                    [:div#container] 
                    (append (transform div-navbar [:div] 
                                       (content div-navbar-inner)))
                    [:div#container] 
                    (append script-jquery script-boot)

                    [:div#container] 
                    )))))

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
  (GET "/" [] (apply str (index)))
  (GET "/login" [] (apply str (emit* login-tmpl)))
  (route/files "")
  (route/not-found "404 baby"))

(def site (handler/site
           (friend/authenticate main-routes
                                {:credential-fn (partial creds/bcrypt-credential-fn lookup)
                                 :workflows [(workflows/interactive-form)]})))

(defn flog [routedef]
  (run-jetty routedef {:port 8080}))


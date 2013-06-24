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
(defn create-tag [tag]
  (list {:tag (keyword tag) :attrs nil :content nil}))  

(def a (create-tag "a"))
(def ul (create-tag "ul"))
(def li (create-tag "li"))
(def script (create-tag "script"))
(def link (create-tag "link"))
(def form (create-tag "form"))
(def label (create-tag "label"))
(def input (create-tag "input"))

;; table defs
;; t = table, r = row, c = column
(def tr (create-tag "tr"))
(def td (create-tag "td"))
(def row-col (transform tr [:tr] (content td)))
(def table (transform (create-tag "table") [:table] (content row-col))) 

;; divs
(def div (create-tag "div"))
(def div-r '({:tag :div :attrs {:class "row"} :content nil}))
(def div-c '({:tag :div :attrs {:class "span6"} :content nil}))

(defn div 
  ([attrs cont]
        (transform (div attrs) [:div] (content cont)))
  ([attrs]
   (if (empty? attrs) (div)
    (let [pair (first attrs)
          k (first pair)
          v (second pair)] 
        (transform (div (rest attrs)) [:div] (set-attr k v)))))
  ([] div (create-tag "div")))

(def div-body (transform div [:div] (set-attr :id "body")))

;; bootstrap 
(def div-container (transform div [:div] (set-attr :class "container-fluid" :id "container")))

(defn li-menu [menu]
  (at li [:li] (content (at a [:a] (set-attr :href (str "/" menu)) [:a] (content (str menu))))))

(def menu-items '("blog","tasks","lessons","resume","code-notes","projects","contact"))

(def menu-list (at ul 
                 [:ul] 
                 (clone-for [i menu-items] (content (li-menu i)))
                 [:ul]
                 (set-attr :class "nav")))

(def brand-link (at a [:a] (set-attr :class "brand" :href "#")
                      [:a] (content "Flog")))

(def login-button (at a [:a] (set-attr :class "btn btn-inverse pull-right" :href "/private")
                          [:a] (content "Login")))

(def div-navbar (transform div [:div] (set-attr :id "nav" :class "navbar navbar-inverse")))
(def div-navbar-inner (at div 
                          [:div] 
                          (set-attr :class "navbar-inner")

                          [:div]
                          (content brand-link menu-list login-button)))

(def menu  (transform div-navbar [:div] (content div-navbar-inner)))

(def link-boot (transform link [:link] 
                          (set-attr :href "bootstrap/css/bootstrap.min.css" 
                                    :rel "stylesheet" 
                                    :media "screen")))

(def script-jquery (transform script [:script] (set-attr :src "http://code.jquery.com/jquery.js")))
(def script-boot (transform script [:script] (set-attr :src "bootstrap/js/bootstrap.min.js")))

(def login-form (at form [:form] (set-attr :class "form-horizontal")
                         [:form] (append (at (div {:class "control-group"}) 
                                              [:div] 
                                              (append (at label [:label] (set-attr :class "control-label" :for "user")
                                                                [:label] (content "Name")))
                                              [:div]
                                              (append (at (div {:class "controls"}) 
                                                          [:div]
                                                          ;; ))))))
                                                          (append (at input [:input] (set-attr :id "username" :type "text" :placeholder "username")))))))))

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
                    (append menu)

                   ;; [:div#container] 
                   ;; (append (transform posts [:div] (set-attr :id "posts")))

                    [:div#container] 
                    (append script-jquery script-boot)
                    
                    )))))

(defn admin [acctid]
  (emit* 
    (at index-tmpl
        [:something])))

(defroutes main-routes
  ;; ======================
  ;; authenticated routes
  ;; ======================
  (context "/private" request
           (friend/wrap-authorize
             (routes
               (GET "/" [] (let [acctid (name ((keyword (str (:identity (friend/current-authentication)))) @user-map))]
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

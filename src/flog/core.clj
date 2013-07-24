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

(defn tag [& {:keys [tag attrs content]
              :or {tag "div" attrs nil content nil}}] 
                {:tag (keyword tag) :attrs attrs :content (if (list? content) content (list content))})

(def div-container 
  (tag :attrs {:class "container-fluid" :id "container"}))

;; NAVBAR ELEMENTS 
(def menu-items '("blog","tasks","lessons","resume","code-notes","projects","contact"))

(defn li-menu [menu]
   (tag :tag "li" 
        :content (tag :tag "a" 
                      :attrs {:href (str "/" menu)} 
                      :content (str menu))))

(defn menu-list [] (map #(list %) menu-items))

(def brand (tag :tag "a" 
                :attrs {:class "brand" :href "#"} 
                :content "Flog"))

(def login-button (tag :tag "a" 
                       :attrs {:class "btn btn-inverse pull-right" :href "/private"}
                       :content "Login"))

;; NAVBAR ASSEMBLY 
(def navbar 
  (->>  (list brand (doall (menu-list)) login-button)
        (tag :attrs {:class "navbar-inner"} :content )
        (tag :attrs {:id "nav" :class "navbar navbar-inverse"} :content )
    ))


;; HEAD ELEMENTS
(def link-bootstrap (tag :tag "link" 
                         :attrs {:href "bootstrap/css/bootstrap.min.css" 
                                 :rel "stylesheet" 
                                 :media "screen"}))

(def script-jquery (tag :tag "script" 
                        :attrs {:src "http://code.jquery.com/jquery.js"}))

(def script-bootstrap (tag :tag "script" 
                           :attrs {:src "bootstrap/js/bootstrap.min.js"}))

;; (def login-form (at form [:form] (set-attr :class "form-horizontal")
;;                          [:form] (append (at (div {:class "control-group"}) 
;;                                               [:div] 
;;                                               (append (at label [:label] (set-attr :class "control-label" :for "user")
;;                                                                 [:label] (content "Name")))
;;                                               [:div]
;;                                               (append (at (div {:class "controls"}) 
;;                                                           [:div]
;;                                                           ;; ))))))
;;                                                           (append (at input [:input] (set-attr :id "username" :type "text" :placeholder "username")))))))))
;;

;; HEAD ASSEMBLY
(def head 
  (->> (list link-bootstrap script-jquery script-bootstrap)
       (tag :tag "head" :content )))

;; BODY ASSEMBLY
(def body 
  (->> navbar
       (tag :tag "body" :content )))

;; HTML ASSEMBLY
(def landing 
  (->> (list head body)
       (tag :tag "html"
            :attrs {:lang "en"}
            :content )))

;; ======================
;; page render
;; ======================

(defn index []
  (apply str (emit* landing)))

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
  (GET "/" [] (index))
;;   (GET "/login" [] (apply str (emit* login-tmpl)))
  (route/files "")
  (route/not-found "404 baby"))

(def site (handler/site
           (friend/authenticate main-routes
                                {:credential-fn (partial creds/bcrypt-credential-fn lookup)
                                 :workflows [(workflows/interactive-form)]})))
(defn flog [routedef]
  (run-jetty routedef {:port 8080}))

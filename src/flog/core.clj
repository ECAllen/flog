(ns flog.core
  (:use compojure.core
        ring.adapter.jetty
        net.cgrand.enlive-html
        flog.html
        flog.blog)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

;; TODO replace this with edn
(def user-file "src/flog/user.clj")

(def users (ref (read-string (slurp user-file))))

;; ============================= 
;; Config
;; ============================= 
;; (def menu-items (list "blog","tasks","lessons","resume","code-notes","projects","contact"))

;; ============================= 
;; Friend funcs
;; ============================= 

;; friend/authentication stuff
(derive ::admin ::user)

;; function used by friend to lookup user for friend workflow
;; (defn lookup [usr] (spit "users.out" @users) (@users usr))

(defn lookup [usr] (@users usr))

;; ======================
;; page render
;; ======================
(defn index []
  (apply str (emit* templt)))

(defn blog-admin []
  (apply str (emit* blog-admin-page)))

(defn login []
  (apply str (emit* login-page)))

(defn blog []
  (apply str (emit* blog-page)))


;; ======================
;; Routes
;; ======================
(defroutes main-routes
  ;; ======================
  ;; authenticated routes
  ;; ======================
  (context "/private" request
           (friend/wrap-authorize
             (routes
               (GET "/blogadmin" [] (blog-admin)) 
               (POST "/add-blog-post" [md title]
                     (post-blog title md (System/currentTimeMillis))
                     (ring.util.response/redirect "/private/blogadmin"))

               (friend/logout (ANY "/logout" request (ring.util.response/redirect "/"))))
            #{::user}))

  ;; ======================
  ;; unauthenticated routes
  ;; ======================
  ;; TODO make routes macro
  (GET "/" [] (index))
  (GET "/blog" [] (blog))
  (GET "/login" [] (login))
  (route/files "")
  (route/not-found "404 baby"))

;; friend authentication workflow
(def site (handler/site
           (friend/authenticate main-routes
                                {:credential-fn (partial creds/bcrypt-credential-fn lookup)
                                 :workflows [(workflows/interactive-form)]})))

;; site handler
(defn flog [routedef]
  (run-jetty routedef {:port 8080}))

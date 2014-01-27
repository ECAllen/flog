(ns flog.html
  (:use net.cgrand.enlive-html))

; ==================================
; experiment with different ways of
; encoding html. Will unify before 
; prod release
; ==================================
;
; ==================================
;; HTML UTILS 
; ==================================
(defn fmt [content] (if (list? content) (flatten content) (flatten (list content))))

; hiccup lib essentially does this but this way
; can have best of hiccup and enlive if needed
(defn tag [& {:keys [tag attrs content]
              :or {tag "div" attrs nil content nil}}] 
                {:tag (keyword tag) :attrs attrs :content (fmt content)})

(def login-form (html-snippet (slurp "src/templates/login.html")))

(def snips (html-snippet (slurp "src/templates/snippets.html")))

(def login-button (select snips [:#login-button]))

(def logout-button (select snips [:#logout-button]))

(def add-blog-post-button (select snips [:#add-blog-post]))

(def templt (html-snippet (slurp "src/templates/template.html")))

(def blog-templt (html-snippet (slurp "src/templates/template.html")))

(def blog-private-templt (at blog-templt [:#login-button] (substitute logout-button)
                                         [:#menu] (append add-blog-post-button )))

(def login-page (at templt [:#content] (append login-form)))

(defproject flog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring "1.1.8"]
                 [org.apache.commons/commons-email "1.2"]
                 [compojure "1.1.5"]
                 [org.clojure/tools.trace "0.7.5"]
                 [enlive "1.1.1"]
                 [com.cemerick/friend "0.1.3"]]
  :plugins [[lein-ring "0.8.3"]
            [lein-marginalia "0.7.1"]]
  ;; :aot :all
  :main flog.core 
  :ring {:handler flog.core/site}
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  :resources-path "public")

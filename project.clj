(defproject flog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-time "0.6.0"]
                 [ring "1.2.1"]
                 [org.apache.commons/commons-email "1.3.2"]
                 [compojure "1.1.6"]
                 [org.clojure/tools.trace "0.7.6"]
                 [enlive "1.1.5"]
                 [com.cemerick/friend "0.2.0"]
                 [markdown-clj "0.9.41"]
                 [com.ashafa/clutch "0.4.0-RC1"]
                 ;; [org.clojure/clojurescript "0.0-2156" :optional true
                 ;; [org.clojure/clojurescript "0.0-1011" :optional true
                 [org.clojure/clojurescript "0.0-1450" :optional true
                  :exclusions [com.google.code.findbugs/jsr305
                               com.googlecode.jarjar/jarjar
                               junit
                               org.apache.ant/ant
                               org.json/json
                               org.mozilla/rhino]]]
  :plugins [[lein-ring "0.8.10"]
            [lein-marginalia "0.7.1"]
            [lein-clean-m2 "0.1.2"]]
  :main flog.core
  :ring {:handler flog.core/site}
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}
             :uberjar {:aot :all}}
  :resources-path "public")

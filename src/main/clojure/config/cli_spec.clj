(ns config.cli-spec
  (:gen-class)
  (:require
   [config.project-template :refer [create-project]]
   [console.ls :refer [list-current-path-files]]
   [plugins.change-jetbrains-path :refer [run-cjp]]
   [plugins.pkg :refer [pkg]]))

(def cmd-info
  {"new"  "create clojure project"
   "cjp"  "change paths in idea.properties"
   "list" "list files in current path"
   "pkg"  "package management with scoop on windows and brew on others"})

(def cli-args
  [{:cmds ["new"] 
    :fn   create-project}
   {:cmds ["cjp"]
    :fn   run-cjp}
   {:cmds ["list"]
    :fn   list-current-path-files}
   {:cmds ["pkg"]
    :fn   pkg}])

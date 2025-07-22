(ns config.cli-spec
  (:gen-class)
  (:require
   [config.project-template :refer [create-project]]
   [console.ls :refer [list-current-path-files]]
   [plugins.change-jetbrains-path :refer [run-cjp]]
   [plugins.pkg :refer [pkg-run]]))

(def cmd-info
  {"new"  "create clojure project"
   "cjp"  "change paths in idea.properties"
   "list" "list files in current path"
   "pkg"  "package management with scoop on windows and brew on others"})

(def list-spec
  {:all {:alias :a
         :desc "list all files include hidden files"
         :coerce :boolean}
   :filename-asc {:alias :fa
                  :desc "sort files by filename ascending"
                  :coerce :boolean}
   :filename-desc {:alias :fd
                   :desc "sort files by filename descending"
                   :coerce :boolean}
   :create-asc {:alias :ca
                :desc "sort files by createtime ascending"
                :coerce :boolean}
   :create-desc {:alias :cd
                 :desc "sort files by createtime descending"
                 :coerce :boolean}
   :type-file {:alias :tf
               :desc "sort files by type ascending"
               :coerce :boolean}
   :type-directory {:alias :td
                    :desc "sort files by type descending"
                    :coerce :boolean}
   :size-asc {:alias :sa
              :desc "sort files by size ascending"
              :coerce :boolean}
   :size-desc {:alias :sd
               :desc "sort files by size descending"
               :coerce :boolean}
   :modify-asc {:alias :ma
                :desc "sort files by moditime ascending"
                :coerce :boolean}
   :modify-desc {:alias :md
                 :desc "sort files by moditime descending"
                 :coerce :boolean}})

(def pkg-spec
  {:self-install {:alias :si
                  :desc "install self"
                  :coerce :boolean}
   :unregistry {:alias :un
                :desc "unregistry shims"
                :coerce :boolean}
   :install {:alias :i
             :desc "install package"}
   :update {:alias :u
            :desc "update package"}
   :remove {:alias :r
            :desc "remove package"}})

(def cli-args
  [{:cmds ["new"]
    :fn   create-project}
   {:cmds ["cjp"]
    :fn   run-cjp}
   {:cmds ["list"]
    :fn   list-current-path-files :spec list-spec}
   {:cmds ["pkg"]
    :fn   pkg-run :spec pkg-spec}])

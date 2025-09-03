(ns config.cli-spec
  (:gen-class)
  (:require
   [babashka.cli :as cli]
   [config.project-template :refer [create-project]]
   [config.debug-control :refer [debug-control]]
   [console.ls :refer [list-current-path-files]]
   [console.rm :refer [run-rm]]
   [console.ocr :refer [run-ocr]]
   [console.kill :refer [run-kill]]
   [console.eol :refer [run-eol]]
   [plugins.change-jetbrains-path :refer [run-cjp]]
   [plugins.pkg :refer [pkg-run]]
   [util.git :refer [clone-repo]]))

(def cmd-info
  {"new"  "create clojure project"
   "cjp"  "change paths in idea.properties"
   "clone" "clone repository from github"
   "debug" "debug enabled/disabled"
   "list" "list files in current path"
   "pkg"  "package management with scoop on windows and brew on others"
   "rm"   "remove files or directories"
   "kill" "kill process by name"
   "ocr"  "ocr image"
   "eol" "change file eol, just like dos2unix"})

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
                  :desc "install package manager, scoop on windows and brew on unix"
                  :coerce :boolean}
   :unregistry {:alias :un
                :desc "unregistry shims"
                :coerce :boolean}
   :install {:alias :i
             :desc "install package, tcl pkg install <package_name>"}
   :update {:alias :u
            :desc "update package, tcl pkg update <package_name>"}
   :remove {:alias :r
            :desc "remove package, tcl pkg remove <package_name>"}
   :clean {:alisas :c
           :desc "clean cache and old version"}})

(def eol-spec
  {:type {:alias :t
          :desc "target eol type, example: --type LF/CRLF"
          :coerce :string}
   :extension {:alias :x
               :desc "file extension, example: txt"
               :coerce :string}
   :hidden {:alias :h
            :desc "available to change hidden dir and file"
            :coerce :boolean}})

(def ocr-spec
  {:type {:alias :t
          :desc "ocr type, local or deepseek, deepseek is default, if use local, you should install tesseract and tesseract-languages"
          :coerce :string}})

(def cli-args
  [{:cmds ["new"]
    :fn   create-project}
   {:cmds ["cjp"]
    :fn   run-cjp}
   {:cmds ["clone"]
    :fn clone-repo}
   {:cmds ["debug"]
    :fn debug-control}
   {:cmds ["list"]
    :fn   list-current-path-files :spec list-spec}
   {:cmds ["pkg"]
    :fn   pkg-run :spec pkg-spec}
   {:cmds ["rm"]
    :fn   run-rm}
   {:cmds ["kill"]
    :fn   run-kill}
   {:cmds ["ocr"]
    :fn   run-ocr :spec ocr-spec}
   {:cmds ["eol"]
    :fn   run-eol :spec eol-spec}])

(defn show-help
  [spec]
  (cli/format-opts (merge spec {:order (vec (keys (:spec spec)))})))

(defn print-help
  "Prints help information for commands."
  ([] (println "Usage: <command> [options]")
      (doseq [[cmd desc] cmd-info]
        (println cmd "\t" desc)))
  ([cmd]
   (if (nil? cmd)
     (print-help)
     (if-let [cmd-spec (first (filter #(= cmd (first (:cmds %))) cli-args))]
       (do
         (println "Usage: tcl" cmd "[options]")
         (println (show-help {:spec (:spec cmd-spec)})))
       (println "Unknown command:" cmd)))))

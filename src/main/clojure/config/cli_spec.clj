(ns config.cli-spec
  (:gen-class)
  (:require
   [config.project-template :refer [create-project]]
   [console.ls :refer [list-current-path-files]]
   [console.rm :refer [run-rm]]
   [plugins.change-jetbrains-path :refer [run-cjp]]
   [plugins.pkg :refer [pkg-run]]
   [util.git :refer [clone-repo]]))

(def cmd-info
  {"new"  "create clojure project"
   "cjp"  "change paths in idea.properties"
   "clone" "clone repository from github"
   "list" "list files in current path"
   "pkg"  "package management with scoop on windows and brew on others"
   "rm"   "remove files or directories"})

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
             :desc "install package, tcl pkg install <package_name>"}
   :update {:alias :u
            :desc "update package, tcl pkg update <package_name>"}
   :remove {:alias :r
            :desc "remove package, tcl pkg remove <package_name>"}
   :clean {:alisas :cl
           :desc "clean cache and old version"}})

(def rm-spec
  {:force {:alias :f
           :desc "ignore nonexistent files and arguments"
           :coerce :boolean}
   :recursive {:alias :r
               :desc "remove directories and their contents recursively"
               :coerce :boolean}})

(def cli-args
  [{:cmds ["new"]
    :fn   create-project}
   {:cmds ["cjp"]
    :fn   run-cjp}
   {:cmds ["clone"]
    :fn clone-repo}
   {:cmds ["list"]
    :fn   list-current-path-files :spec list-spec}
   {:cmds ["pkg"]
    :fn   pkg-run :spec pkg-spec}
   {:cmds ["rm"]
    :fn   run-rm :spec rm-spec}])

(defn print-command-options
  "Prints the options for a given command spec, with aligned columns."
  [cmd spec]
  (if (empty? spec)
    (println "No options available for this command.")
    (do
      (println "Usage: " cmd " [options]")
      (let [max-key-len (apply max (map (comp count name first) spec))]
        (doseq [[key {:keys [desc]}] spec]
          (let [key-str (name key)
                padding (apply str (repeat (- max-key-len (count key-str)) " "))]
            (println key-str padding "\t" desc)))))))

(defn print-help
  "Prints help information for commands."
  ([] (println "Usage: <command> [options]")
      (doseq [[cmd desc] cmd-info]
        (println cmd "\t" desc)))
  ([cmd]
   (if (nil? cmd)
     (print-help)
     (if-let [cmd-spec (first (filter #(= cmd (first (:cmds %))) cli-args))]
       (print-command-options cmd (:spec cmd-spec))
       (println "Unknown command:" cmd)))))

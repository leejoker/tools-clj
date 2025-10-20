(ns util.draw-table
  (:gen-class)
  (:import [java.lang Character Character$UnicodeBlock])
  (:require
   [clojure.string :as s]))

(defrecord DrawTable [header body max-width])

(defn repeat-str
  [s n]
  (apply str (repeat n s)))

(defn s-len
  [^String s]
  (let [str-len (count (.getBytes s))]
    (if (= str-len 1)
      1
      2)))

(defn column-width
  [^String column-value]
  (let [column (if (s/includes? column-value "\033[0m")
                 (s/replace column-value #"\033\[\d+m" "")
                 column-value)]
    (letfn [(char-wide? [^Integer codePoint]
              (let [charType      (Character/getType codePoint)
                    unicode-block (Character$UnicodeBlock/of codePoint)]
                (or (= unicode-block Character$UnicodeBlock/CJK_UNIFIED_IDEOGRAPHS)
                    (= charType Character/OTHER_LETTER)
                    (= charType Character/LETTER_NUMBER)
                    (<= 0x4e00 codePoint 0x9fff)
                    (<= 0x3000 codePoint 0x303f)
                    (<= 0xff00 codePoint 0xffef))))]
      (reduce (fn [width codePoint]
                (if (char-wide? codePoint)
                  (+ width 2)
                  (+ width 1)))
              0
              (int-array (.toArray (.codePoints column)))))))

(defn column-sub
  [^String s n total-size result]
  (let [array (.split s "")
        cur (first array)]
    (if (> (+ total-size (s-len cur)) n)
      result
      (recur (apply str (rest array)) n (+ total-size (s-len cur)) (str result cur)))))

(defn line-widths
  [line]
  (map #(column-width %) line))

(defn cal-max-width
  [line1 line2 result]
  (let [cur1 (first line1)
        cur2 (first line2)
        r (if (nil? result) [] result)]
    (if (empty? line1)
      (seq result)
      (recur (rest line1) (rest line2) (conj r (if (> cur1 cur2) cur1 cur2))))))

(defn add-row
  [table row]
  (let [table-max-width (:max-width table)
        row-widths (line-widths row)
        result-max-width (cal-max-width table-max-width row-widths nil)]
    (assoc table :max-width result-max-width)))

(defn create-table
  [header body]
  (let [init-table (DrawTable. header body (line-widths header))
        table      (reduce #(add-row %1 %2) init-table body)]
    table))

(defn draw-divider
  [table]
  (let [table-width (apply + (:max-width table))
        total-width (+ table-width (* (count (:max-width table)) 5) 1)]
    (println (repeat-str "=" total-width))))

(defn draw-line
  [max-width line result]
  (let [space (repeat-str " " 2)
        line-value (first line)
        width-value (first max-width)]
    (if (empty? line)
      (str result "|")
      (recur (rest max-width) (rest line) (str (if (nil? result) "" result)
                                               "|"
                                               space
                                               line-value
                                               (repeat-str " " (- width-value (column-width line-value)))
                                               space)))))

(defn draw-table
  [table]
  (draw-divider table)
  (println (draw-line (:max-width table) (:header table) nil))
  (draw-divider table)
  (dotimes [idx (count (:body table))] (println (draw-line (:max-width table) (nth (:body table) idx) nil)))
  (draw-divider table))

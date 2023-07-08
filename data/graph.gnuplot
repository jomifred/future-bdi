#!/opt/local/bin/gnuplot -persist
set datafile separator ","
#set title "Simulation Result"
set xlabel "change probability"
#set ylabel "actions to solve"
set key top left
set term pdfcairo
set output "solve-f-time.pdf"

set y2tics
set ytics nomirror
set y2label "# actions"
set y2range [20:60]

plot "stats-g3-solve-f-90.csv" using 2:10 title "time" smooth sbezier,\
     "stats-g3-solve-f-90.csv" using 2:9 title "actions" smooth sbezier axis x1y2

#set output "solve-f-actions.pdf"
#plot "stats-g3-solve-f-90.csv" using 2:9 title "actions" smooth sbezier

set output "solve-f-states.pdf"
plot "stats-g3-solve-f-90.csv" using 2:8 title "states" smooth sbezier,\
     "stats-g3-solve-f-90.csv" using 2:9 title "actions" smooth sbezier axis x1y2

set output "solve-f-matrices.pdf"
set yrange [0:4000]
plot "stats-g3-solve-f-90.csv" using 2:7 title "matrices" smooth sbezier,\
     "stats-g3-solve-f-90.csv" using 2:9 title "actions" smooth sbezier axis x1y2

#     "stats-g3-solve-f-90.csv" using 2:9 title "actions"  smooth sbezier,\
#     "data-j.csv"  title "Jason"    smooth sbezier,\
#     "data-er2.csv" title "JasonER-with-done" smooth sbezier

# smooth option sbezier csplines

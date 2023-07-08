#!/opt/local/bin/gnuplot -persist
set datafile separator ","
#set title "Simulation Result"
set xlabel "change probability (p)"
set ylabel "# actions"
set key top left

set terminal pdfcairo
#set terminal png

set y2tics
set ytics nomirror
set yrange [120:27] reverse

file="stats-g3-solve-f-90-10walls.csv"

#eff(x)=120-x

set output "solve-f-time.pdf"
set y2label "# time"
plot file using 2:9 title "efficiency" smooth sbezier,\
     file using 2:10 title "time" smooth sbezier axis x1y2

set output "solve-f-states.pdf"
set y2label "# states"
plot file using 2:9 title "efficiency" smooth sbezier,\
     file using 2:8 title "states" smooth sbezier axis x1y2


set output "solve-f-matrices.pdf"
set y2range [0:20000]
set y2label "# matrices"
plot file using 2:9 title "efficiency" smooth sbezier, \
     file using 2:7 title "cost" smooth sbezier axis x1y2,\


#     "stats-g3-solve-f-90.csv" using 2:9 title "actions"  smooth sbezier,\
#     "data-j.csv"  title "Jason"    smooth sbezier,\
#     "data-er2.csv" title "JasonER-with-done" smooth sbezier

# smooth option sbezier csplines

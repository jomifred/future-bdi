#!/opt/local/bin/gnuplot -persist
set datafile separator ","
#set title "Simulation Result"
set xlabel "probability of change (p)"
set ylabel "# actions"
set key top right

set terminal pdfcairo
#set terminal png

set y2tics
set ytics nomirror
set yrange [60:27] reverse

file="stats-g3-solve-f90-5walls-final.csv"

#eff(x)=120-x

set output "g-solve-f-time.pdf"
set y2label "# time"
set y2range [700:4500]
plot file using 2:9 title "efficiency" smooth sbezier,\
     file using 2:10 title "time" smooth sbezier axis x1y2

set output "g-solve-f-states.pdf"
set y2label "# states"
#set logscale y2
set y2range [1:8000000]
plot file using 2:9 title "efficiency" smooth sbezier,\
     file using 2:8 title "states" axis x1y2 smooth sbezier 


set output "g-solve-f-matrices.pdf"
set y2range [30:1500]
set y2label "# matrices"
set logscale y2
set xrange [0.0:1.0]
plot file using 2:9 title "efficiency" smooth sbezier, \
     file using 2:7 title "cost" smooth sbezier axis x1y2
     #"stats-g3-random90-5walls-final.csv" using 2:9 title "random agent" smooth sbezier, \

#     "stats-g3-solve-f-90.csv" using 2:9 title "actions"  smooth sbezier,\
#     "data-j.csv"  title "Jason"    smooth sbezier,\
#     "data-er2.csv" title "JasonER-with-done" smooth sbezier

# smooth option sbezier csplines

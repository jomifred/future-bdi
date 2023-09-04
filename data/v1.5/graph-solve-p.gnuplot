#!/opt/local/bin/gnuplot -persist
set title "(rc=0.9)"

set datafile separator ","
set xlabel "probability of change (p)"
set ylabel "# actions"
set key top right

set terminal pdfcairo
#set terminal png

set y2tics
set ytics nomirror
set yrange [70:25] reverse

#fileP="stats-g3-solve-p90-5walls.csv"
#fileF="stats-g3-solve-f90-5walls-final.csv"
fileP="stats-g3-solvep-5walls-v2.csv"
fileF="stats-g3-solvef-5walls-v2.csv"

set output "g-solve-p-visited.pdf"

#set y2range [900:4500] # for time
#set y2label "# time (ms)"

set y2range [1:3500] # for states
set y2label "# visited states"
#set logscale y2

set xrange [0.0:1.0]
plot fileP using 2:($5 == 0.9 ? $9 : 1/0) title "efficiency P" smooth sbezier, \
     fileF using 2:($5 == 0.9 ? $9 : 1/0) title "efficiency F" smooth sbezier, \
     "stats-g3-random-5walls.csv" using 2:9 title "efficiency of no recovery" smooth sbezier, \
     fileP using 2:($5 == 0.9 ? $8 : 1/0) title "cost P" smooth sbezier axis x1y2, \
     fileF using 2:($5 == 0.9 ? $8 : 1/0) title "cost F" smooth sbezier axis x1y2

#plot file using 2:9 title "efficiency" smooth sbezier, \
#     file using 2:7 title "cost" smooth sbezier axis x1y2
#     #"stats-g3-random90-5walls-final.csv" using 2:9 title "random agent" smooth sbezier, \

#     "stats-g3-solve-f-90.csv" using 2:9 title "actions"  smooth sbezier,\
#     "data-j.csv"  title "Jason"    smooth sbezier,\
#     "data-er2.csv" title "JasonER-with-done" smooth sbezier

#!/opt/local/bin/gnuplot -persist
set datafile separator ","
set datafile missing "NaN"
#unset datafile

fileM="stats-g3-solve-m-5walls-v3.csv"
fileR="stats-g3-random-5walls-v3.csv"

#set title "(solve-m)"

set xlabel "probability of change (p)"
set ylabel "# actions"
set key top right

set terminal pdfcairo
#set terminal png

set y2tics
set ytics nomirror
set yrange [170:17] reverse
set y2range[0:13000]
set xrange [0.0:1.0]


#set output "graphs/solve-m-states.pdf"
#set y2label "# visited states"
#plot fileM using 2:($5 == 0.9 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.9" smooth sbezier with lines,\
#     fileM using 2:($5 == 0.7 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.7" smooth sbezier with lines,\
#     fileM using 2:($5 == 0.5 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.5" smooth sbezier with lines,\
#     fileM using 2:($5 == 0.3 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.3" smooth sbezier with lines,\
#     fileM using 2:($5 == 0.1 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.1" smooth sbezier with lines,\
#     fileM using 2:($5 == 0.9 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.9" axis x1y2 smooth sbezier,\
#     fileM using 2:($5 == 0.7 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.7" axis x1y2 smooth sbezier,\
#     fileM using 2:($5 == 0.5 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.5" axis x1y2 smooth sbezier,\
#     fileM using 2:($5 == 0.3 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.3" axis x1y2 smooth sbezier,\
#     fileM using 2:($5 == 0.1 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.1" axis x1y2 smooth sbezier,\
#     fileM using 2:($5 == 0.9 && strcol(11) eq "timeout" ? 500 : 0) axis x1y2 smooth freq with line title "timeout 0.9",\
#     fileM using 2:($5 == 0.5 && strcol(11) eq "timeout" ? 500 : 0) axis x1y2 smooth freq with line title "timeout 0.5",\
#     fileM using 2:($5 == 0.1 && strcol(11) eq "timeout" ? 500 : 0) axis x1y2 smooth freq with line title "timeout 0.1"

set y2label "# visited states"

#RCS = "1.0 0.95 0.9 0.7 0.5 0.3 0.1"
RCS = "1.0 0.9 0.7 0.5 0.3"
do for [rc in RCS] {
    set key bottom right
    set output "graphs/solve-m-states-".rc.".pdf"
    plot fileM using 2:($5 == rc && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency rc=".rc smooth sbezier with line lw 2 ,\
         fileM using 2:($5 == rc && strcol(11) eq "ontime" ? $8 : 1/0) title "cost rc=".rc axis x1y2 smooth sbezier with line lw 2,\
         "stats-g3-solve-m-5walls-v3-to.csv" using 2:($1 == rc ? $3*10000/($3+$4) : 1/0) axis x1y2 title "efficacy rc=".rc smooth sbezier with line dashtype 5 lw 2,\
         fileR using 2:(strcol(11) eq "ontime"? $9 : 1/0) title "efficiency random" smooth sbezier with line dashtype 3 lw 2
}

set output "graphs/solve-m-a-eff.pdf"
set key bottom right
unset y2label
unset y2tics
plot for [rc in RCS] fileM using 2:($5 == rc && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency rc=".rc smooth sbezier with line lw 2,\
     fileR using 2:(strcol(11) eq "ontime"? $9 : 1/0) title "efficiency random" smooth sbezier with line dashtype 3 lw 2


set output "graphs/solve-m-a-cost.pdf"
unset ylabel
unset ytics
set key top right
set y2label "# visited states"
set y2tics
#set y2range[0:13000]
plot for [rc in RCS] fileM using 2:($5 == rc && strcol(11) eq "ontime" ? $8 : 1/0) title "cost rc=".rc axis x1y2 smooth sbezier with line lw 2

set output "graphs/solve-m-a-success.pdf"
set y2range[0:140]
set y2label "% of goal achievement"
plot for [rc in RCS] "stats-g3-solve-m-5walls-v3-to.csv" using 2:($1 == rc ? $3*100/($3+$4) : 1/0) axis x1y2 title "efficacy rc=".rc smooth sbezier with line dashtype 5 lw 2

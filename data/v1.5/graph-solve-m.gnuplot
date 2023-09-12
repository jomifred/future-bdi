#!/opt/local/bin/gnuplot -persist
set datafile separator ","
set datafile missing "NaN"
#unset datafile

version="v4"
fileM  ="runs/stats-g3-solve-m-5walls-".version.".csv"
fileR  ="runs/stats-g3-random-5walls-".version.".csv"
fileTOM="runs/stats-g3-solve-m-5walls-".version."-to.csv"

#set title "(solve-m)"

set xlabel "probability of change (p)"
set ylabel "path length"
set key top right

set terminal pdfcairo
#set terminal png

set y2tics
set ytics nomirror
set yrange [170:17] reverse
set y2range[0:13000]
set xrange [0.0:1.0]

set y2label "# visited states"

#RCS = "1.0 0.95 0.9 0.7 0.5 0.3 0.1"
#RCS = "1.0 0.9 0.7 0.5 0.3"
RCS = "0.9"
do for [rc in RCS] {
    set key bottom right
    set output "graphs/solve-m-states-".rc."-".version.".pdf"
    plot fileM   using 2:($5 == rc && strcol(11) eq "ontime" ? $12 : 1/0) title "efficiency rc=".rc smooth sbezier with line lw 2 ,\
         fileM   using 2:($5 == rc && strcol(11) eq "ontime" ? $8 : 1/0) title "cost rc=".rc axis x1y2 smooth sbezier with line lw 2,\
         fileTOM using 2:($1 == rc ? $3*10000/($3+$4) : 1/0) axis x1y2 title "effectiveness rc=".rc smooth sbezier with line dashtype 5 lw 2,\
         fileR   using 2:(strcol(11) eq "ontime"? $12 : 1/0) title "efficiency random" smooth sbezier with line dashtype 3 lw 2,\
         "runs/stats-g3-random-5walls-v3.csv"   using 2:(strcol(11) eq "ontime"? $9 : 1/0) title "efficiency v3 random" smooth sbezier with line dashtype 4 lw 2
}

set output "graphs/solve-m-a-eff-".version.".pdf"
set key bottom right
unset y2label
unset y2tics
plot for [rc in RCS] fileM using 2:($5 == rc && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency rc=".rc smooth sbezier with line lw 2,\
     fileR using 2:(strcol(11) eq "ontime"? $9 : 1/0) title "efficiency random" smooth sbezier with line dashtype 3 lw 2


set output "graphs/solve-m-a-cost-".version.".pdf"
unset ylabel
unset ytics
set key top right
set y2label "# visited states"
set y2tics
#set y2range[0:13000]
plot for [rc in RCS] fileM using 2:($5 == rc && strcol(11) eq "ontime" ? $8 : 1/0) title "cost rc=".rc axis x1y2 smooth sbezier with line lw 2

set output "graphs/solve-m-a-success-".version.".pdf"
set y2range[0:140]
set y2label "% of goal achievement"
plot for [rc in RCS] fileTOM using 2:($1 == rc ? $3*100/($3+$4) : 1/0) axis x1y2 title "effectiveness rc=".rc smooth sbezier with line dashtype 5 lw 2

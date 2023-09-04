#!/opt/local/bin/gnuplot -persist
set datafile separator ","
set datafile missing "NaN"
#unset datafile

#file="stats-g3-solve-f90-5walls-final.csv"
file="stats-g3-solvef-5walls-v3.csv"

#set title "(solve-f)"

set xlabel "probability of change (p)"
set ylabel "# actions"
set key top right

set terminal pdfcairo
#set terminal png

set output "x.pdf"
to9=0
ot9=0
stats file using 2:($5 == 0.9 && strcol(11) eq "timeout" ? (to9=to9+1, 1) : 0) nooutput
stats file using 2:($5 == 0.9 && strcol(11) eq "ontime"  ? (ot9=ot9+1, 1) : 0) nooutput
print to9
print ot9

to5=0
ot5=0
stats file using 2:($5 == 0.5 && strcol(11) eq "timeout" ? (to5=to5+1, 1) : 0) nooutput
stats file using 2:($5 == 0.5 && strcol(11) eq "ontime"  ? (ot5=ot5+1, 1) : 0) nooutput
print to5
print ot5

plot file using 2:($5 == 0.9 && strcol(11) eq "timeout" ? 100./(to9+ot9) : 0) smooth freq with line,\
     file using 2:($5 == 0.5 && strcol(11) eq "timeout" ? 100./(to5+ot5) : 0) smooth freq with line


set y2tics
set ytics nomirror
set yrange [170:17] reverse
set xrange [0.0:1.0]


#eff(x)=120-x

set output "g-solve-f-time.pdf"
set y2label "# time"
set y2range [700:4500]
plot file using 2:9 title "efficiency" smooth sbezier,\
     file using 2:10 title "time" smooth sbezier axis x1y2

set output "g-solve-f-plans.pdf"
set y2range [0:7]
set y2label "# plans"
plot file using 2:9 title "efficiency" smooth sbezier, \
     file using 2:6 title "cost" smooth sbezier axis x1y2


set output "g-solve-f-states.pdf"
set y2label "# visited states"
#set logscale y2
set y2range [1:13500]
plot file using 2:($5 == 0.9 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.9" smooth sbezier,\
     file using 2:($5 == 0.7 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.7" smooth sbezier,\
     file using 2:($5 == 0.5 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.5" smooth sbezier,\
     file using 2:($5 == 0.3 && strcol(11) eq "ontime" ? $9 : 1/0) title "efficiency 0.3" smooth sbezier,\
     file using 2:($5 == 0.9 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.9" axis x1y2 smooth sbezier,\
     file using 2:($5 == 0.7 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.7" axis x1y2 smooth sbezier,\
     file using 2:($5 == 0.5 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.5" axis x1y2 smooth sbezier,\
     file using 2:($5 == 0.3 && strcol(11) eq "ontime" ? $8 : 1/0) title "cost 0.3" axis x1y2 smooth sbezier,\
     file using 2:($5 == 0.9 && strcol(11) eq "timeout" ? 500 : 0) axis x1y2 smooth freq with line title "timeout 0.9",\
     file using 2:($5 == 0.5 && strcol(11) eq "timeout" ? 500 : 0) axis x1y2 smooth freq with line title "timeout 0.5"

set output "g-solve-f-states2.pdf"
plot file using 2:($5 == 0.9 && strcol(11) eq "ontime" ? $9 : 1/0) smooth sbezier


set output "g-solve-f-matrices.pdf"
set y2range [30:1500]
set y2label "# matrices"
set logscale y2
plot file using 2:9 title "efficiency" smooth sbezier, \
     file using 2:7 title "cost" smooth sbezier axis x1y2
     #"stats-g3-random90-5walls-final.csv" using 2:9 title "random agent" smooth sbezier, \


#     "stats-g3-solve-f-90.csv" using 2:9 title "actions"  smooth sbezier,\
#     "data-j.csv"  title "Jason"    smooth sbezier,\
#     "data-er2.csv" title "JasonER-with-done" smooth sbezier

# smooth option sbezier csplines

#!/opt/local/bin/gnuplot -persist

set datafile separator ","
set xtics rotate out
set style data histogram
set style fill solid border

set terminal pdfcairo
set output "x.pdf"
set style histogram rowstacked
#set boxwidth 0.6 relative
plot  'to.csv' using (100.*$2/($2+$3)):xtic(1) t column(1)

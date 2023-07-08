cd ../..

./gradlew build

rm examples/stats.csv
kotlin -cp examples/build/libs/examples.jar:build/libs/jason-f-1.4.jar \
   example.tools.RunGrid3AllKt \
   SOLVE_F 0.9

#cp examples/stats.csv data/stats-g3-solve-f.csv
cp data/stats-g3-solve-f-90.csv data/stats-g3-solve-f-90.csv.old
cat examples/stats.csv >> data/stats-g3-solve-f-90.csv

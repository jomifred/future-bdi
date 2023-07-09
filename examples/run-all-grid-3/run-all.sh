cd ../..

./gradlew build

SOURCE=examples/stats.csv
RESULT=data/stats-g3-solve-f90-5walls.csv
touch $SOURCE
touch $RESULT
rm $SOURCE

kotlin -cp examples/build/libs/examples.jar:build/libs/jason-f-1.4.jar \
   example.tools.RunGrid3AllKt \
   SOLVE_F 0.9

cp $RESULT $RESULT.old
cat $SOURCE >> $RESULT

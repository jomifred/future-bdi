cd ../..

./gradlew build

SOURCE=examples/stats.csv
RESULT=data/v1.5/stats-g3-random-5walls-v3.csv
touch $SOURCE
touch $RESULT
rm $SOURCE

kotlin -cp examples/build/libs/examples.jar:build/libs/jason-f-1.5.jar \
   example.tools.RunGrid3AllKt \
   RANDOM 0.9
cp $RESULT $RESULT.old
cat $SOURCE >> $RESULT

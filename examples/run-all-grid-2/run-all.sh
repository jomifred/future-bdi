cd ../..

./gradlew build

SOURCE=examples/stats.txt
RESULT=data/v1.4/stats-g2.txt
touch $SOURCE
touch $RESULT
rm $SOURCE

kotlin -cp examples/build/libs/examples.jar:build/libs/jason-f-1.5.jar \
   example.tools.RunGrid2AllKt

cp $RESULT $RESULT.old
cp $SOURCE $RESULT

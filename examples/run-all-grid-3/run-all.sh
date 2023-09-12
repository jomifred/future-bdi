cd ../..

./gradlew build

S=random
V=v4
RC=0.9

SOURCE=examples/stats.csv
RESULT=data/v1.5/runs/stats-g3-$S-5walls-$V.csv
touch $SOURCE
touch $RESULT
rm $SOURCE

kotlin -cp examples/build/libs/examples.jar:build/libs/jason-f-1.5.jar \
   example.tools.RunGrid3AllKt \
   $S $RC
cp $RESULT $RESULT.old
cat $SOURCE >> $RESULT

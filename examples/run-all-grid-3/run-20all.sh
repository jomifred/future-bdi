

D=`pwd`
for i in `seq 1 20`
do
  echo "******* run: $i"
  ./run-all.sh
  cd $D
done

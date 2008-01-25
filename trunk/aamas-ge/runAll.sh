#!/bin/sh
for i in `seq 0 399`;
do
  echo running with params run$i.params
  java -jar gep.jar -file params/run$i.params
done    

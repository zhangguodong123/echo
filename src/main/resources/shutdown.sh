#!/bin/sh
#PID=$(ps auxf|grep "echo-1.1.0-fat.jar" |grep -v grep|awk '{print $2}')
#echo "kill $PID"
#kill $PID
PID=`cat pid.txt`
echo "kill $PID"
kill $PID


#!/bin/sh
source /etc/profile
cd /data/wwwroot/echo.adsense.cig.com.cn
PID=$(ps auxf|grep "echo-1.1.0-fat.jar" |grep -v grep|awk '{print $2}')
echo "kill $PID"
kill $PID
sleep 3
nohup java -Djava.net.preferIPv4Stack=true -server -Xms16g -Xmx16g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:InitiatingHeapOccupancyPercent=70 -XX:ParallelGCThreads=15 -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -Xloggc:/data/wwwlogs/echo.adsense.cig.com.cn/g1gc-`date +'%Y-%m-%d_%H-%M-%S'`.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/wwwlogs/echo.adsense.cig.com.cn -XX:ErrorFile=/data/wwwlogs/echo.adsense.cig.com.cn/jvm-error-`date +'%Y-%m-%d_%H-%M-%S'`.log -jar echo-1.1.0-fat.jar > echoServer-`date +'%Y-%m-%d_%H-%M-%S'`.log 2>&1&
echo $! > pid.txt


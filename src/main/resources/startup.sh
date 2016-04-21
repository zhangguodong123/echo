#!/bin/sh
nohup java -Djava.net.preferIPv4Stack=true -server -Xms16g -Xmx16g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:InitiatingHeapOccupancyPercent=70 -XX:ParallelGCThreads=15 -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -Xloggc:/data/wwwlogs/echo.adsense.cig.com.cn/g1gc-`date +'%Y-%m-%d_%H-%M-%S'`.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/wwwlogs/echo.adsense.cig.com.cn -XX:ErrorFile=/data/wwwlogs/echo.adsense.cig.com.cn/jvm-error-`date +'%Y-%m-%d_%H-%M-%S'`.log -jar echo-1.1.0-fat.jar > echoServer-`date +'%Y-%m-%d_%H-%M-%S'`.log 2>&1&
echo $! > pid.txt


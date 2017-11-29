@echo off
title SAF-Worker
java -classpath ..\conf;..\lib\*  com.ipd.jsf.worker.start.AppStartup
pause
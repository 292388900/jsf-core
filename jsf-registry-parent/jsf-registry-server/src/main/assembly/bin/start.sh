#!/bin/sh

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`
echo current path:$BASEDIR

BASEBIN_DIR=$BASEDIR"/bin"
cd $BASEBIN_DIR

SAF_INSTANCE="saf01"
SAF_PIDPATH="$BASEBIN_DIR"

GC_DATE=`date +%Y-%m-%d-%H-%M`

LOG_PATH="/export/Logs/saf21registry.jd.local"
JVM_FILE="-XX:+UseCondCardMark -XX:+UseConcMarkSweepGC -XX:+HeapDumpOnOutOfMemoryError "
JVM_FILE="$JVM_FILE -XX:CMSWaitDuration=250"
JVM_FILE="$JVM_FILE -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:${LOG_PATH}/gc-${GC_DATE}.log"
JVM_FILE="$JVM_FILE -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M"
JVM_FILE="$JVM_FILE -XX:HeapDumpPath=${LOG_PATH}/ -XX:ErrorFile=${LOG_PATH}/java_error-${GC_DATE}.log"


if [ "$1" != "" ] && [ "$2" != "" ]; then
    SAF_INSTANCE="$1"
fi

if [ "$3" != "" ]; then
    SAF_PIDPATH="$3"
fi

# ------ check if server is already running
PIDFILE=$SAF_PIDPATH"/"$SAF_INSTANCE"_startup.pid"
if [ -f $PIDFILE ]; then
    if kill -0 `cat $PIDFILE` > /dev/null 2>&1; then
        echo server already running as process `cat $PIDFILE`.
        exit 0
    fi
fi

# ------ set JAVACMD
# If a specific java binary isn't specified search for the standard 'java' binary
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

# ------ set CLASSPATH
CLASSPATH="$BASEDIR/conf/:$BASEDIR/lib/*"
echo "$CLASSPATH"

# ------ set jvm memory
if [ -z "$OPTS_MEMORY" ] ; then
    #OPTS_MEMORY="-Xms2048m -Xmx2048m -Xmn800m"
    OPTS_MEMORY="`sed -n '1p' jvm.properties`"
fi

if [ "`sed -n '2p' jvm.properties`" != "" ] ; then
    JAVACMD="`sed -n '2p' jvm.properties`"
fi

#DEBUG_OPTS="-Dcom.sun.management.jmxremote.port=20712 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false "
#DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 "

#JPDA_OPTS="-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n"
# ------ run proxy
#nohup "$JAVACMD" $JAVA_OPTS $JPDA_OPTS \
nohup "$JAVACMD" $JPDA_OPTS \
  $OPTS_MEMORY $DEBUG_OPTS $JVM_FILE \
  -classpath "$CLASSPATH" \
  -Dbasedir="$BASEDIR" \
  -Dfile.encoding="UTF-8" \
  -Dsaf_instance="$SAF_INSTANCE" \
  com.ipd.jsf.registry.start.RegsitryStartup \
  > /dev/null &


# ------ wirte pid to file
if [ $? -eq 0 ]
then
    if /bin/echo -n $! > "$PIDFILE"
    then
        sleep 1
        echo STARTED SUCCESS
    else
        echo FAILED TO WRITE PID
        exit 1
    fi
#    tail -100f $LOGFILE
else
    echo SERVER DID NOT START
    exit 1
fi

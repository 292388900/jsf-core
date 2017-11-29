#!/bin/sh
BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`
echo current path $BASEDIR

SAF_INSTANCE="saf01"
SAF_PIDPATH="$BASEDIR"

if [ "$1" != "" ]; then
    SAF_INSTANCE="$1"
fi

if [ "$2" != "" ]; then
    SAF_PIDPATH="$2"
fi

PIDFILE=$SAF_PIDPATH"/"$SAF_INSTANCE"_startup.pid"
echo $PIDFILE

if [ ! -f "$PIDFILE" ]
then
    echo "no registry to stop (could not find file $PIDFILE)"
else
    kill $(cat "$PIDFILE")
    sleep 10
    kill -9 $(cat "$PIDFILE")
    rm -f "$PIDFILE"
    echo STOPPED
fi
exit 0

echo stop finished.

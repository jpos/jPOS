#! /bin/sh

# $Id$

QSPBASE=`pwd`/`dirname $0`/
cd `dirname $0`/../../../../../..

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find 'java'. check your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi

JAVA=$JAVA_HOME/bin/java

CLASSPATH=./build/classes:./build/examples:$CLASSPATH
CLASSPATH=`echo ext/*.jar | tr ' ' ':'`:$CLASSPATH
CLASSPATH=$JAVA_HOME/lib/tools.jar:$CLASSPATH

$JAVA -cp $CLASSPATH -Djpos.config=src/etc/jpos.cfg \
	-Dsax.parser=org.apache.xerces.parsers.SAXParser \
	org.jpos.apps.qsp.QSP $QSPBASE/$1


@echo off
REM $Id$

cd ../../../../../..
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\examples;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (ext\*.jar) do call cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

%JAVA% -classpath %CP% -Djpos.config=src/etc/jpos.cfg -Dsax.parser=org.apache.xerces.parsers.SAXParser org.jpos.apps.qsp.QSP src/ext/org/jpos/apps/qsp/qsp-config.xml

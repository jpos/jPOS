@echo off
REM $Id$
set JAVA=%JAVA_HOME%\bin\java
set CLASSPATH=build\classes;build\examples;%CLASSPATH%
set cp=%CLASSPATH%
for %%i in (ext\*.jar) do call cp.bat %%i
for %%i in (lib\*.jar) do call cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

%JAVA% -classpath %CP% -Djpos.config=src/etc/jpos.cfg -Dsax.parser=org.apache.xerces.parsers.SAXParser %1.Test %2 %3 %4 %5 %6



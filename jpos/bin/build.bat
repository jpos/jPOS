@echo off
REM $Id$
set JAVA=%JAVA_HOME%\bin\java
set cp=%CLASSPATH%
for %%i in (ext\*.jar) do call bin\cp.bat %%i
for %%i in (lib\*.jar) do call bin\cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%
%JAVA% -classpath "%CP%" -Dant.home=lib org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 -buildfile src/build.xml
set CP=

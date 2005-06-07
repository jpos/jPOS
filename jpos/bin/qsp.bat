@echo off
REM $Id$
set JAVA=%JAVA_HOME%\bin\java
set cp=build\classes;build\examples;lib\xercesImpl-2.6.2.jar;lib\xalan_2_0_0.jar;lib\log4j.jar;lib\mx4j-jmx.jar;lib\mx4j-tools.jar;lib\mx4j-impl.jar;lib\jdom.jar%CLASSPATH%
for %%i in (ext\*.jar) do call bin\cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

%JAVA% -classpath %CP% org.jpos.apps.qsp.QSP %1

set cp=

@echo off
set JAVA=%JAVA_HOME%\bin\java
set CP=%CLASSPATH%;q2.jar;./build/classes;./build/examples
for %%i in (lib\*.jar) do call bin\cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar
%JAVA% -classpath %CP% org.jpos.q2.Q2 %1

set cp=

@echo off
set JAVA=%JAVA_HOME%\bin\java
set CP=%CLASSPATH%;./build/classes
for %%i in (lib\*.jar) do call bin\cp.bat %%i
for %%i in (ext\*.jar) do call bin\cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar
%JAVA% -classpath %CP% org.jpos.q2.Q2 %1

set cp=

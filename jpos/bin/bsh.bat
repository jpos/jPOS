@echo off
REM $Id$
set JAVA=%JAVA_HOME%\bin\java
set cp=build\classes;build\examples;%CLASSPATH%
for %%i in (lib\*.jar) do call bin\cp.bat %%i
for %%i in (ext\*.jar) do call bin\cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

%JAVA% -classpath %CP%  bsh.Interpreter %1 %2 %3 %4 %5

set cp=

@echo off
REM $Revision$ $Date$
REM Console for JCE Security Module
REM example 1: bin\console.bat -lmk src/ext-examples/smadapter/lmk ck 128 tpk 99C2B0C250C7190DC5B11D1B28F5C007
REM example 1: this should return the check value: 01D863 (givin that you have not changed or rebuilt the LMK's)
set JAVA=%JAVA_HOME%\bin\java
set cp=build\classes;build\examples;lib\xerces_1_2_3.jar;lib\xalan_2_0_0.jar;lib\log4j.jar;lib\jdom.jar;%CLASSPATH%
for %%i in (ext\*.jar) do call bin\cp.bat %%i
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

%JAVA% -classpath %CP% org.jpos.security.jceadapter.Console %1 %2 %3 %4 %5 %6 %7 %8 %9

set cp=

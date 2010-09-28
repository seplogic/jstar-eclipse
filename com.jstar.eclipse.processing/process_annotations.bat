@echo off
set JAR_PROCESSING=C:\Users\da319\workspace\jstar\com.jstar.eclipse.processing\jar file\jstar_processing.jar
set JAR_LIB=C:\Users\da319\workspace\jstar\com.jstar.eclipse.processing\lib\commons-io-1.4\commons-io-1.4.jar
set JAR_ANNOT=C:\Users\da319\workspace\jstar\com.jstar.eclipse.annotations\jar file\annotations.jar
set JARS="%CLASSPATH%;.;%JAR_PROCESSING%;%JAR_LIB%;%JAR_ANNOT%"
set PROC=com.jstar.eclipse.processing.SpecAnnotationProcessor
echo on

javac -proc:only -cp %JARS% -d %1 -processor %PROC% %2
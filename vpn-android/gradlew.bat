@if "%DEBUG%" == "" @echo off
@rem
@rem  Gradle start script for Windows
@rem
@setlocal
set DIR=%~dp0
if "%DIR%" == "" set DIR=.
set JAVA_EXE=java.exe
"%JAVA_EXE%" -classpath "%DIR%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

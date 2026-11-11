@echo off
cd /d "%~dp0"
echo Iniciando Baozi Store API...
call mvn spring-boot:run
set "resultado=%errorlevel%"
pause
exit /b %resultado%

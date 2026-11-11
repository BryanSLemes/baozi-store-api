@echo off
cd /d "%~dp0"
echo Executando os testes automatizados...
call mvn -B clean verify
set "resultado=%errorlevel%"
pause
exit /b %resultado%

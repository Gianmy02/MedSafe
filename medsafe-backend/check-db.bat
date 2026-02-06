@echo off
REM Script per verificare lo stato del database e degli utenti

echo.
echo ========================================
echo   VERIFICA DATABASE MEDSAFE
echo ========================================
echo.

REM Trova il container MySQL
for /f "tokens=*" %%i in ('docker ps --filter "publish=3307" --format "{{.Names}}"') do set MYSQL_CONTAINER=%%i

if "%MYSQL_CONTAINER%"=="" (
    echo [ERRORE] Nessun container MySQL trovato sulla porta 3307
    pause
    exit /b 1
)

echo [INFO] Container MySQL: %MYSQL_CONTAINER%
echo.

REM Verifica database
echo [1/3] Verifica esistenza database...
docker exec %MYSQL_CONTAINER% mysql -u root -proot -e "SHOW DATABASES LIKE 'medsafe';" 2>nul
echo.

REM Verifica tabelle
echo [2/3] Tabelle nel database medsafe:
docker exec %MYSQL_CONTAINER% mysql -u root -proot medsafe -e "SHOW TABLES;" 2>nul
echo.

REM Verifica utenti
echo [3/3] Utenti nella tabella users:
docker exec %MYSQL_CONTAINER% mysql -u root -proot medsafe -e "SELECT id, email, full_name, role, enabled FROM users;" 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] Tabella users non esiste ancora
    echo Possibili cause:
    echo   1. Spring Boot non e stato avviato dopo il reset del DB
    echo   2. Spring Boot e in esecuzione ma non ha ancora creato le tabelle
    echo.
    echo Soluzione: Riavvia Spring Boot con: .\mvnw.cmd spring-boot:run
)
echo.

echo ========================================
echo   VERIFICA COMPLETATA
echo ========================================
echo.
pause

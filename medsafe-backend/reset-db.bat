@echo off
REM Script per reinizializzare il database MySQL su Docker
REM Questo elimina tutti i dati e ricrea il database da zero

echo.
echo ========================================
echo   REINIZIALIZZAZIONE DATABASE MEDSAFE
echo ========================================
echo.

REM Trova il nome del container MySQL
for /f "tokens=*" %%i in ('docker ps --filter "publish=3307" --format "{{.Names}}"') do set MYSQL_CONTAINER=%%i

if "%MYSQL_CONTAINER%"=="" (
    echo [ERRORE] Nessun container MySQL trovato sulla porta 3307
    echo Verifica che Docker sia avviato con: docker-compose up -d
    pause
    exit /b 1
)

echo [INFO] Container MySQL trovato: %MYSQL_CONTAINER%
echo.

REM Elimina il database
echo [1/3] Eliminazione database medsafe...
docker exec %MYSQL_CONTAINER% mysql -u root -proot -e "DROP DATABASE IF EXISTS medsafe;"
if %errorlevel% neq 0 (
    echo [ERRORE] Impossibile eliminare il database
    pause
    exit /b 1
)
echo [OK] Database eliminato

REM Ricrea il database
echo [2/3] Creazione nuovo database medsafe...
docker exec %MYSQL_CONTAINER% mysql -u root -proot -e "CREATE DATABASE medsafe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% neq 0 (
    echo [ERRORE] Impossibile creare il database
    pause
    exit /b 1
)
echo [OK] Database creato

REM Verifica
echo [3/3] Verifica database...
docker exec %MYSQL_CONTAINER% mysql -u root -proot -e "SHOW DATABASES LIKE 'medsafe';"
echo.

echo ========================================
echo   REINIZIALIZZAZIONE COMPLETATA!
echo ========================================
echo.
echo Ora riavvia Spring Boot per ricreare le tabelle e inserire i dati:
echo   ./mvnw.cmd spring-boot:run
echo.
echo Oppure se gia avviato, fermalo (Ctrl+C) e riavvialo.
echo.
pause

@echo off
REM resource-test.bat - Test AWS resource allocation locally (Windows Batch version)

if "%1" neq "--test" (
    echo Usage: resource-test.bat --test
    echo This will start a resource-constrained environment to simulate AWS limits
    exit /b 1
)

echo 🚀 Starting AWS Resource Simulation Test...

REM Create monitoring configuration
if not exist "monitoring\grafana\dashboards" mkdir monitoring\grafana\dashboards

echo global: > monitoring\prometheus.yml
echo   scrape_interval: 15s >> monitoring\prometheus.yml
echo   evaluation_interval: 15s >> monitoring\prometheus.yml
echo. >> monitoring\prometheus.yml
echo scrape_configs: >> monitoring\prometheus.yml
echo   - job_name: 'prometheus' >> monitoring\prometheus.yml
echo     static_configs: >> monitoring\prometheus.yml
echo       - targets: ['localhost:9090'] >> monitoring\prometheus.yml
echo. >> monitoring\prometheus.yml
echo   - job_name: 'cadvisor' >> monitoring\prometheus.yml
echo     static_configs: >> monitoring\prometheus.yml
echo       - targets: ['cadvisor:8080'] >> monitoring\prometheus.yml
echo. >> monitoring\prometheus.yml
echo   - job_name: 'spring-boot' >> monitoring\prometheus.yml
echo     static_configs: >> monitoring\prometheus.yml
echo       - targets: ['api-gateway:3000', 'user-service:4020', 'group-service:4040'] >> monitoring\prometheus.yml
echo     metrics_path: '/actuator/prometheus' >> monitoring\prometheus.yml

echo Starting resource-constrained environment...

REM Start the stack
docker-compose -f docker-compose.aws-simulation.yml up -d

if %errorlevel% neq 0 (
    echo ❌ Failed to start Docker Compose
    exit /b 1
)

echo Waiting for services to be ready...
timeout /t 30 /nobreak > nul

REM Check service health
echo ⏳ Checking API Gateway health...
:check_api_gateway
curl -f http://localhost:3000/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo    Waiting for API Gateway...
    timeout /t 10 /nobreak > nul
    goto check_api_gateway
)
echo ✅ API Gateway is healthy!

echo ⏳ Checking User Service health...
:check_user_service
curl -f http://localhost:4020/actuator/health > nul 2>&1
if %errorlevel% neq 0 (
    echo    Waiting for User Service...
    timeout /t 10 /nobreak > nul
    goto check_user_service
)
echo ✅ User Service is healthy!

REM Show resource usage
echo.
echo 📊 Current Resource Usage:
echo =========================
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" | findstr /C:"api-gateway" /C:"user-service" /C:"group-service" /C:"postgres" /C:"redis" /C:"keycloak"

echo.
echo 🔥 Running Load Tests...

REM Simple load test using PowerShell
powershell -Command "1..50 | ForEach-Object { Start-Job -ScriptBlock { try { Invoke-WebRequest -Uri 'http://localhost:3000/actuator/health' -UseBasicParsing -TimeoutSec 5 } catch { } } } | Wait-Job | Remove-Job"

powershell -Command "1..50 | ForEach-Object { Start-Job -ScriptBlock { try { Invoke-WebRequest -Uri 'http://localhost:4020/actuator/health' -UseBasicParsing -TimeoutSec 5 } catch { } } } | Wait-Job | Remove-Job"

echo Load test completed!

echo.
echo 📊 Resource Usage After Load Test:
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" | findstr /C:"api-gateway" /C:"user-service" /C:"group-service" /C:"postgres" /C:"redis" /C:"keycloak"

echo.
echo 🎯 Test Results:
echo ================
echo ✅ Check Prometheus metrics at: http://localhost:9090
echo ✅ Check Grafana dashboard at: http://localhost:3001 (admin/admin)
echo ✅ Check Glances monitor at: http://localhost:61208
echo ✅ Check cAdvisor at: http://localhost:8081
echo.
echo 💡 To analyze results:
echo    1. Look for any OOMKilled containers: docker ps -a
echo    2. Check container logs: docker logs ^<container-name^>
echo    3. Monitor memory usage in Grafana
echo    4. Run: docker stats --no-stream for live stats

echo.
echo Press any key to stop the environment...
pause > nul

echo 🧹 Cleaning up...
docker-compose -f docker-compose.aws-simulation.yml down
docker system prune -f

echo Environment stopped!
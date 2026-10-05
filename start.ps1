# 1. Start PostgreSQL via Docker Compose
Write-Host "Starting PostgreSQL container..." -ForegroundColor Green
docker-compose up -d

# 2. Pause brief moment for database initialization
Start-Sleep -Seconds 5

# 3. Launch Spring Boot in a new background terminal window
Write-Host "Launching Spring Boot Backend..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd Backend\pipeline; .\mvnw.cmd spring-boot:run"

# 4. Launch React Vite frontend in a new background terminal window
Write-Host "Launching Vite Frontend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; npm run dev"

Write-Host "All services launching! Check opened terminal windows for logs." -ForegroundColor Green
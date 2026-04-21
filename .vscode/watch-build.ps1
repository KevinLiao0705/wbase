# 監控 src/ 目錄變更，自動執行 mvn package -DskipTests
param(
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

$srcPath = Join-Path $ProjectRoot "src"

if (-not (Test-Path $srcPath)) {
    Write-Host "[ERROR] src 目錄不存在: $srcPath" -ForegroundColor Red
    exit 1
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Maven Auto Build - 儲存時自動打包 WAR" -ForegroundColor Cyan
Write-Host " 監控目錄: $srcPath" -ForegroundColor Cyan
Write-Host " 按 Ctrl+C 停止" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan

$lastBuild  = [DateTime]::MinValue
$debounceSec = 2   # 防抖：同一秒內多次儲存只觸發一次

$watcher = New-Object System.IO.FileSystemWatcher
$watcher.Path                  = $srcPath
$watcher.IncludeSubdirectories = $true
$watcher.EnableRaisingEvents   = $true

Write-Host "Watching for changes..." -ForegroundColor Green

while ($true) {
    $change = $watcher.WaitForChanged([System.IO.WatcherChangeTypes]::All, 500)

    if (-not $change.TimedOut) {
        $now = Get-Date
        if (($now - $lastBuild).TotalSeconds -gt $debounceSec) {
            $lastBuild = $now
            Write-Host ""
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] 偵測到變更: $($change.Name)" -ForegroundColor Yellow
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] 執行 mvn package ..." -ForegroundColor Cyan

            Push-Location $ProjectRoot
            mvn package -DskipTests
            $exitCode = $LASTEXITCODE
            Pop-Location

            if ($exitCode -eq 0) {
                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] BUILD SUCCESS - WAR 已更新" -ForegroundColor Green
            } else {
                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] BUILD FAILED (exit: $exitCode)" -ForegroundColor Red
            }

            Write-Host "Watching for changes..." -ForegroundColor Green
        }
    }
}

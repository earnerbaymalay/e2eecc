# Cypherchat - JDK 17 Setup Script for Windows
# This script downloads and installs Eclipse Temurin JDK 17 (Adoptium)

$JDK_VERSION = "17.0.13"
$JDK_BUILD = "11"
$JDK_URL = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-$JDK_VERSION%2B$JDK_BUILD/OpenJDK17U-jdk_x64_windows_hotspot_$JDK_VERSION`_$JDK_BUILD.zip"
$INSTALL_DIR = "C:\Java\jdk-17"
$DOWNLOAD_FILE = "$env:TEMP\openjdk17.zip"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Cypherchat JDK 17 Setup" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$skipDownload = $false
if (Test-Path "$INSTALL_DIR\bin\java.exe") {
    Write-Host "[INFO] JDK 17 already installed at: $INSTALL_DIR" -ForegroundColor Green
    & "$INSTALL_DIR\bin\java.exe" -version
    Write-Host ""
    $response = Read-Host "Reinstall? (y/N)"
    if ($response -ne 'y' -and $response -ne 'Y') {
        Write-Host "[SKIP] Using existing installation" -ForegroundColor Yellow
        $skipDownload = $true
    }
}

if (-not $skipDownload) {
    Write-Host "[1/4] Downloading Eclipse Temurin JDK 17..." -ForegroundColor Yellow
    Write-Host "      URL: $JDK_URL"
    try {
        Invoke-WebRequest -Uri $JDK_URL -OutFile $DOWNLOAD_FILE -UseBasicParsing
        Write-Host "      [OK] Downloaded to: $DOWNLOAD_FILE" -ForegroundColor Green
    } catch {
        Write-Host "      [ERROR] Download failed!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please manually download JDK 17 from:" -ForegroundColor Yellow
        Write-Host "https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Choose: Windows x64, JDK, .zip file" -ForegroundColor Yellow
        exit 1
    }

    Write-Host "[2/4] Creating install directory..." -ForegroundColor Yellow
    if (Test-Path $INSTALL_DIR) {
        Remove-Item $INSTALL_DIR -Recurse -Force
    }
    New-Item -ItemType Directory -Path $INSTALL_DIR -Force | Out-Null
    Write-Host "      [OK] Directory created: $INSTALL_DIR" -ForegroundColor Green

    Write-Host "[3/4] Extracting JDK (this may take a minute)..." -ForegroundColor Yellow
    try {
        Expand-Archive -Path $DOWNLOAD_FILE -DestinationPath "C:\Java" -Force
        $extractedFolder = Get-ChildItem -Path "C:\Java" -Directory | Where-Object { $_.Name -like "jdk-17*" } | Select-Object -First 1
        if ($extractedFolder) {
            Get-ChildItem -Path $extractedFolder.FullName | Move-Item -Destination $INSTALL_DIR -Force
            Remove-Item $extractedFolder.FullName -Force
        }
        Write-Host "      [OK] Extracted to: $INSTALL_DIR" -ForegroundColor Green
    } catch {
        Write-Host "      [ERROR] Extraction failed: $_" -ForegroundColor Red
        exit 1
    }

    Remove-Item $DOWNLOAD_FILE -Force -ErrorAction SilentlyContinue
}

Write-Host "[4/4] Configuring environment variables..." -ForegroundColor Yellow
[Environment]::SetEnvironmentVariable("JAVA_HOME", $INSTALL_DIR, "User")
Write-Host "      [OK] JAVA_HOME set to: $INSTALL_DIR" -ForegroundColor Green

$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$javaPath = "$INSTALL_DIR\bin"
if ($currentPath -notlike "*$javaPath*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$javaPath", "User")
    Write-Host "      [OK] Added to PATH: $javaPath" -ForegroundColor Green
} else {
    Write-Host "      [OK] Already in PATH" -ForegroundColor Green
}

$env:JAVA_HOME = $INSTALL_DIR
$env:Path = "$env:Path;$javaPath"

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Installation Complete!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Verifying Java installation:" -ForegroundColor Yellow
& "$INSTALL_DIR\bin\java.exe" -version

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "IMPORTANT: Restart your terminal/IDE for" -ForegroundColor Yellow
Write-Host "environment variables to take effect!" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Close and reopen your terminal" -ForegroundColor White
Write-Host "  2. Run: java -version" -ForegroundColor White
Write-Host "  3. Run: cd $((Get-Location).Path)" -ForegroundColor White
Write-Host "  4. Run: .\gradlew.bat build" -ForegroundColor White
Write-Host ""

# Configure GitLab Remote Helper Script
# This script helps you add a GitLab remote to your repository

param(
    [string]$GitLabUrl = "",
    [string]$Username = "",
    [string]$ProjectName = "e2eecc",
    [switch]$UseSSH = $false
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  GitLab Remote Configuration" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Get GitLab URL if not provided
if ([string]::IsNullOrWhiteSpace($GitLabUrl)) {
    Write-Host "Enter your GitLab instance URL:" -ForegroundColor Yellow
    Write-Host "  - For GitLab.com: gitlab.com" -ForegroundColor Gray
    Write-Host "  - For self-hosted: your-company.gitlab.com" -ForegroundColor Gray
    $GitLabUrl = Read-Host "GitLab URL"
}

# Get username/group if not provided
if ([string]::IsNullOrWhiteSpace($Username)) {
    Write-Host ""
    Write-Host "Enter your GitLab username or group name:" -ForegroundColor Yellow
    $Username = Read-Host "Username/Group"
}

# Ask about SSH vs HTTPS
if (-not $UseSSH) {
    Write-Host ""
    Write-Host "Choose connection method:" -ForegroundColor Yellow
    Write-Host "  1. SSH (git@gitlab.com:username/project.git)" -ForegroundColor Gray
    Write-Host "  2. HTTPS (https://gitlab.com/username/project.git)" -ForegroundColor Gray
    $choice = Read-Host "Enter choice (1 or 2)"
    $UseSSH = ($choice -eq "1")
}

# Construct the remote URL
if ($UseSSH) {
    if ($GitLabUrl -eq "gitlab.com") {
        $RemoteUrl = "git@gitlab.com:$Username/$ProjectName.git"
    } else {
        $RemoteUrl = "git@$GitLabUrl`:$Username/$ProjectName.git"
    }
} else {
    if ($GitLabUrl -eq "gitlab.com") {
        $RemoteUrl = "https://gitlab.com/$Username/$ProjectName.git"
    } else {
        $RemoteUrl = "https://$GitLabUrl/$Username/$ProjectName.git"
    }
}

Write-Host ""
Write-Host "Configuring GitLab remote with URL:" -ForegroundColor Cyan
Write-Host "  $RemoteUrl" -ForegroundColor White
Write-Host ""

# Check if gitlab remote already exists
$existingRemote = git remote get-url gitlab 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "GitLab remote already exists: $existingRemote" -ForegroundColor Yellow
    $response = Read-Host "Replace it? (y/N)"
    if ($response -eq 'y' -or $response -eq 'Y') {
        git remote remove gitlab
        Write-Host "Removed existing GitLab remote" -ForegroundColor Green
    } else {
        Write-Host "Keeping existing remote. Exiting." -ForegroundColor Yellow
        exit 0
    }
}

# Add the remote
git remote add gitlab $RemoteUrl

if ($LASTEXITCODE -eq 0) {
    Write-Host "GitLab remote added successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Current remotes:" -ForegroundColor Cyan
    git remote -v
    Write-Host ""
    Write-Host "To test the connection, run:" -ForegroundColor Yellow
    Write-Host "  git fetch gitlab" -ForegroundColor White
    Write-Host ""
    Write-Host "To push to GitLab:" -ForegroundColor Yellow
    Write-Host "  git push gitlab <branch-name>" -ForegroundColor White
} else {
    Write-Host "Failed to add GitLab remote. Please check the URL and try again." -ForegroundColor Red
    exit 1
}


param(
    [switch]$Configure
)

$ErrorActionPreference = 'Stop'

function Read-EmailAddress([string]$Prompt) {
    while ($true) {
        $candidate = (Read-Host $Prompt).Trim()
        try {
            $address = [System.Net.Mail.MailAddress]::new($candidate)
            if ($address.Address -ceq $candidate) {
                return $address.Address
            }
        } catch [System.FormatException] {
        }
        Write-Warning 'Enter a valid email address and try again.'
    }
}

function Convert-SecureStringToPlainText([Security.SecureString]$Value) {
    $pointer = [IntPtr]::Zero
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    } finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
    }
}

$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1
if ($listener) {
    $process = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
    $processName = if ($process) { $process.ProcessName } else { 'unknown process' }
    Write-Error "Port 8080 is already in use by $processName (PID $($listener.OwningProcess)). Stop the existing backend from its terminal with Ctrl+C, then run this launcher again. The existing backend cannot receive SMTP settings from a second launch."
    exit 1
}

if ([string]::IsNullOrWhiteSpace($env:APPDATA)) {
    throw 'The Windows user AppData location is unavailable; cannot securely save SMTP settings.'
}

$configDirectory = Join-Path $env:APPDATA 'SanelePortfolioPlatform'
$configPath = Join-Path $configDirectory 'gmail-smtp.dpapi'

if (-not $Configure -and (Test-Path -LiteralPath $configPath)) {
    try {
        $encryptedSettings = (Get-Content -LiteralPath $configPath -Raw).Trim()
        $secureSettings = ConvertTo-SecureString -String $encryptedSettings
        $settingsJson = Convert-SecureStringToPlainText $secureSettings
        $settings = ConvertFrom-Json -InputObject $settingsJson
        if ([string]::IsNullOrWhiteSpace($settings.SmtpEmail) -or
            [string]::IsNullOrWhiteSpace($settings.NotificationEmail) -or
            [string]::IsNullOrWhiteSpace($settings.AppPassword)) {
            throw 'Saved SMTP settings are incomplete.'
        }
    } catch {
        throw "Saved SMTP settings could not be read or decrypted for this Windows account. Run this script with -Configure to save them again. Details: $($_.Exception.Message)"
    }
} else {
    Write-Host 'Enter your Gmail SMTP settings. They will be saved encrypted for this Windows account.'
    $smtpEmail = Read-EmailAddress 'Google email address linked to your App Password'
    $notificationEmail = Read-EmailAddress 'Email address that should receive new portfolio messages'
    $securePassword = Read-Host 'Google App Password (input is hidden)' -AsSecureString
    $appPassword = [regex]::Replace((Convert-SecureStringToPlainText $securePassword), '\s', '')
    $securePassword.Dispose()

    if ([string]::IsNullOrWhiteSpace($appPassword)) {
        throw 'The App Password cannot be empty.'
    }

    $settingsJson = [ordered]@{
        SmtpEmail = $smtpEmail
        NotificationEmail = $notificationEmail
        AppPassword = $appPassword
    } | ConvertTo-Json -Compress
    $secureSettings = ConvertTo-SecureString -String $settingsJson -AsPlainText -Force
    $encryptedSettings = ConvertFrom-SecureString -SecureString $secureSettings

    New-Item -ItemType Directory -Path $configDirectory -Force | Out-Null
    $temporaryConfigPath = "$configPath.tmp"
    Set-Content -LiteralPath $temporaryConfigPath -Value $encryptedSettings -Encoding ASCII
    Move-Item -LiteralPath $temporaryConfigPath -Destination $configPath -Force
    Write-Host 'SMTP settings saved using Windows encryption for your current Windows account.'

    $settings = ConvertFrom-Json -InputObject $settingsJson
    $appPassword = $null
    $settingsJson = $null
    $secureSettings.Dispose()
}

$env:SPRING_MAIL_HOST = 'smtp.gmail.com'
$env:SPRING_MAIL_PORT = '587'
$env:SPRING_MAIL_SMTP_AUTH = 'true'
$env:SPRING_MAIL_STARTTLS = 'true'
$env:SPRING_MAIL_USERNAME = $settings.SmtpEmail
$env:PORTFOLIO_MAIL_FROM = $settings.SmtpEmail
$env:PORTFOLIO_NOTIFICATION_EMAIL = $settings.NotificationEmail
$env:SPRING_MAIL_PASSWORD = [regex]::Replace($settings.AppPassword, '\s', '')

Write-Host 'Starting the backend with the saved Gmail SMTP settings.'
Set-Location $PSScriptRoot
try {
    & .\mvnw.cmd spring-boot:run
    $backendExitCode = $LASTEXITCODE
} finally {
    Remove-Item Env:SPRING_MAIL_PASSWORD -ErrorAction SilentlyContinue
}

Write-Host "Backend process exited with code $backendExitCode."
Read-Host 'Press Enter to close this setup terminal' | Out-Null
exit $backendExitCode

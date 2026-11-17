# Loads local server settings without printing their values.
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $projectDir '.env'
if (!(Test-Path -LiteralPath $envFile)) { throw 'Create .env from .env.example first.' }
foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
# Portable tools are optional and remain ignored by Git.
$portableJava = Get-ChildItem (Join-Path $projectDir '.tools') -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue | Select-Object -First 1
if ($portableJava -and !(Get-Command java -ErrorAction SilentlyContinue)) { $env:JAVA_HOME = $portableJava.FullName }
Push-Location (Join-Path $projectDir 'backend')
try { & .\mvnw.cmd spring-boot:run; if ($LASTEXITCODE -ne 0) { throw 'Backend startup failed.' } }
finally { Pop-Location }

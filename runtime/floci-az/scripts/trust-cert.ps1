# Downloads the floci-az self-signed certificate and installs it in the current
# user's trust store (no admin rights required). Prerequisite: FLOCI_AZ_TLS_ENABLED=true.
$ErrorActionPreference = "Stop"
$ProgressPreference    = "SilentlyContinue"

$out = Join-Path $PSScriptRoot "floci-az.crt"
Invoke-WebRequest -Uri "http://localhost:4577/_floci/tls-cert" -OutFile $out -UseBasicParsing

# With TLS disabled the endpoint answers JSON instead of a PEM certificate.
$body = Get-Content $out -Raw
if ($body -notlike "*BEGIN CERTIFICATE*") {
    throw "floci-az did not return a certificate (TLS disabled?). Check FLOCI_AZ_TLS_ENABLED=true."
}

Import-Certificate -FilePath $out -CertStoreLocation Cert:\CurrentUser\Root | Out-Null

Write-Host "floci-az certificate installed in Cert:\CurrentUser\Root"

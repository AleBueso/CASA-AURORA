# Convierte el reporte de JaCoCo (target/site/jacoco/jacoco.xml) al formato Istanbul
# y lo guarda en coverage/coverage-summary.json.
# Uso: primero .\mvnw verify y después:
#   powershell -ExecutionPolicy Bypass -File scripts\cobertura.ps1

$origen = "target/site/jacoco/jacoco.xml"
if (-not (Test-Path $origen)) {
    Write-Error "No existe $origen. Corré primero: .\mvnw verify"
    exit 1
}

# Se quita la línea DOCTYPE para leer el XML sin buscar archivos externos
$texto = (Get-Content $origen -Raw) -replace '<!DOCTYPE[^>]*>', ''
[xml]$xml = $texto

function Resumen([string]$tipo) {
    $contador = $xml.report.counter | Where-Object { $_.type -eq $tipo }
    $cubiertas = [int]$contador.covered
    $total = $cubiertas + [int]$contador.missed
    $pct = if ($total -gt 0) { [math]::Round(100 * $cubiertas / $total, 2) } else { 100 }
    return [ordered]@{ total = $total; covered = $cubiertas; skipped = 0; pct = $pct }
}

$resumen = [ordered]@{
    total = [ordered]@{
        lines      = Resumen 'LINE'
        statements = Resumen 'INSTRUCTION'
        functions  = Resumen 'METHOD'
        branches   = Resumen 'BRANCH'
    }
}

New-Item -ItemType Directory -Force -Path coverage | Out-Null
$destino = Join-Path (Get-Location) "coverage/coverage-summary.json"
$json = $resumen | ConvertTo-Json -Depth 5
[IO.File]::WriteAllText($destino, $json, (New-Object System.Text.UTF8Encoding $false))

Write-Host "Reporte guardado en coverage/coverage-summary.json"
Write-Host "Cobertura de lineas: $($resumen.total.lines.pct)% ($($resumen.total.lines.covered) de $($resumen.total.lines.total))"
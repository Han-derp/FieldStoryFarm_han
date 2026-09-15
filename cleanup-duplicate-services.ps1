$dir = "src/main/java/com/fieldstory/farm/service/economy/impl"
if (-not (Test-Path $dir)) {
    Write-Host "[SKIP] $dir not found."
    exit 0
}
Get-ChildItem $dir -Filter "Basic*.java" -File | Remove-Item -Force
$extra = Join-Path $dir "CropMemoryFactRecorder.java"
if (Test-Path $extra) { Remove-Item $extra -Force }
Write-Host "[OK] Duplicate Basic service copies removed. EconomyServiceImpl.java was kept."

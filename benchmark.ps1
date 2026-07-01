# Redis 缓存性能对比脚本
# 用法：启动项目后，在项目根目录 PowerShell 中运行 .\benchmark.ps1
# 第一次运行（冷启动/缓存过期后）：测无缓存时的响应时间
# 紧接着第二次运行：测缓存命中后的响应时间

$base = "http://localhost:8080"

Write-Host "========== Redis 缓存性能对比 ==========" -ForegroundColor Cyan
Write-Host ""

# 1. 用户公开统计缓存 — 先清除再测
Write-Host ">>> 用户公开统计 /api/user/1/stats" -ForegroundColor Yellow
redis-cli -p 6380 DEL userStats::1 2>$null | Out-Null
$cold1 = Measure-Command { Invoke-WebRequest -Uri "$base/api/user/1/stats" -UseBasicParsing | Out-Null }
Write-Host "  无缓存（查DB）: $([math]::Round($cold1.TotalMilliseconds))ms"

$warm1 = Measure-Command { Invoke-WebRequest -Uri "$base/api/user/1/stats" -UseBasicParsing | Out-Null }
Write-Host "  缓存命中:       $([math]::Round($warm1.TotalMilliseconds))ms"

# 2. 运动项目缓存
Write-Host ""
Write-Host ">>> 运动项目 /api/projects" -ForegroundColor Yellow
redis-cli -p 6380 DEL sportProject::enabled 2>$null | Out-Null
$cold2 = Measure-Command { Invoke-WebRequest -Uri "$base/api/projects" -UseBasicParsing | Out-Null }
Write-Host "  无缓存（查DB）: $([math]::Round($cold2.TotalMilliseconds))ms"

$warm2 = Measure-Command { Invoke-WebRequest -Uri "$base/api/projects" -UseBasicParsing | Out-Null }
Write-Host "  缓存命中:       $([math]::Round($warm2.TotalMilliseconds))ms"

# 3. 勋章缓存
Write-Host ""
Write-Host ">>> 勋章列表 /api/medals/progress" -ForegroundColor Yellow
redis-cli -p 6380 DEL medal::all 2>$null | Out-Null
$cold3 = Measure-Command { Invoke-WebRequest -Uri "$base/api/medals/progress" -UseBasicParsing | Out-Null }
Write-Host "  无缓存（查DB）: $([math]::Round($cold3.TotalMilliseconds))ms"

$warm3 = Measure-Command { Invoke-WebRequest -Uri "$base/api/medals/progress" -UseBasicParsing | Out-Null }
Write-Host "  缓存命中:       $([math]::Round($warm3.TotalMilliseconds))ms"

# 总结
Write-Host ""
Write-Host "========== 总结 ==========" -ForegroundColor Cyan
Write-Host "用户统计: $([math]::Round($cold1.TotalMilliseconds))ms -> $([math]::Round($warm1.TotalMilliseconds))ms (提升 $([math]::Round(($cold1.TotalMilliseconds - $warm1.TotalMilliseconds) / [Math]::Max($cold1.TotalMilliseconds, 1) * 100))%)"
Write-Host "运动项目: $([math]::Round($cold2.TotalMilliseconds))ms -> $([math]::Round($warm2.TotalMilliseconds))ms (提升 $([math]::Round(($cold2.TotalMilliseconds - $warm2.TotalMilliseconds) / [Math]::Max($cold2.TotalMilliseconds, 1) * 100))%)"
Write-Host "勋章列表: $([math]::Round($cold3.TotalMilliseconds))ms -> $([math]::Round($warm3.TotalMilliseconds))ms (提升 $([math]::Round(($cold3.TotalMilliseconds - $warm3.TotalMilliseconds) / [Math]::Max($cold3.TotalMilliseconds, 1) * 100))%)"

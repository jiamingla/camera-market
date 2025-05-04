# --- 在本機端做測試都需要先執行這段powershell script ---

# 你的 Google Cloud 專案 ID
$env:GOOGLE_CLOUD_PROJECT = "camera-market-cloud" 

# 你的 Cloud SQL 實例連接名稱 (格式: project-id:region:instance-id)
$env:DB_INSTANCE_CONNECTION_NAME = "camera-market-cloud:asia-east1:camera-market" 

# 你的資料庫密碼 (從 Cloud SQL 實例設定中獲取)
$env:DB_PASS = ""

# 你的資料庫名稱 (如果不是 'camera_market'，請修改)
$env:DB_NAME = "postgres" 

# 你的資料庫使用者名稱 (如果不是 'postgres'，請修改)
$env:DB_USER = "postgres" 

# --- 檢查變數是否設置成功 (可選) ---
Write-Host "DB_INSTANCE_CONNECTION_NAME: $($env:DB_INSTANCE_CONNECTION_NAME)"
Write-Host "DB_PASS set: $($env:DB_PASS -ne $null -and $env:DB_PASS -ne '')" # 只檢查是否已設置，不顯示密碼
Write-Host "DB_NAME: $($env:DB_NAME)"
Write-Host "DB_USER: $($env:DB_USER)"
Write-Host "GOOGLE_CLOUD_PROJECT: $($env:GOOGLE_CLOUD_PROJECT)" # Socket Factory 可能會用到

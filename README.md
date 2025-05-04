# Camera.Market - 電子商務相機交易平台

## 簡介

Camera.Market 是一個基於 Spring Boot, Vue 框架開發的電子商務平台，專門用於相機和相關配件的線上交易。這個平台提供了使用者註冊、登入、瀏覽、發布、編輯和刪除商品等功能。  
https://camera-market-cloud.web.app
目前後端服務部署在 Google Cloud Run，前端服務部署在 Google Firebase Hosting，資料庫使用GCP的託管服務Cloud SQL，預計搬到 GCP 的 VM Compute Engine 裡面放一個DB，因為費用相對便宜很多。
## 主要功能

*   **使用者管理：**
    *   註冊：使用者可以透過提供使用者名稱、密碼和電子郵件來註冊帳戶。
    *   登入：使用者可以使用其使用者名稱和密碼登入平台。
    *   個人資料：使用者可以查看自己的資訊。
*   **商品管理：**
    *   瀏覽商品：使用者可以瀏覽平台上的所有商品。
    *   發布商品：已登入的使用者可以發布新的商品。
*   **安全機制：**
    *   JWT (JSON Web Tokens) 認證：使用 JWT 來確保 API 的安全存取。
    *   密碼加密：使用 PasswordEncoder 來儲存加密過的密碼。

## 使用方法

### 前置準備

1.  **Java 開發環境：** 確保已安裝 JDK (Java Development Kit) 17 或以上版本。
2.  **Maven：** 雖然可以使用 Maven Wrapper，但建議也安裝 Maven，方便執行其他 Maven 命令。
3. **資料庫:** 需要設置資料庫，並且在 `application.properties` 檔案中設定資料庫的連線資訊。

### 執行步驟

1.  **切換到專案根目錄**
2.  **編譯與測試：** 執行以下命令來編譯和測試專案：
    ```bash
    ./mvnw clean install  # (Unix/Linux/macOS)
    mvnw.cmd clean install # (Windows)
    ```
3.  **執行應用程式：** 執行以下命令來啟動應用程式：
    ```bash
    ./mvnw spring-boot:run  # (Unix/Linux/macOS)
    mvnw.cmd spring-boot:run # (Windows)
    ```
    或者你可以直接執行 `CameraMarketApplication.java` 這個類別，裡面有一個 main 方法，可以直接執行。
4. **使用 Maven Daemon(mvnd)**:
    1. 更改`.mvn/wrapper/maven-wrapper.properties` 中 `distributionUrl` 的值為 `maven-mvnd-*` 開頭的網址，例如：`distributionUrl=https\://repo.maven.apache.org/maven2/org/apache/maven/mvnd/maven-mvnd-0.9.0-m2/maven-mvnd-0.9.0-m2-bin.zip`
    2. 使用 `mvnw clean install` 或 `mvnw spring-boot:run` 即可使用 mvnd 來加速構建。

## 開發規劃

*   專案上CI，Branch merge前要過test
*   完善商品管理功能，增加商品的圖片、規格等資訊。
*   增加搜尋和篩選商品的功能。
*   優化使用者介面和體驗。
*   增加後台管理功能。
* ...等等

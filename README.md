# Camera Market - 相機交易平台 (Spring Boot 後端)

## 簡介

Camera Market 是一個基於 Spring Boot 框架開發的電子商務平台後端，專門用於相機和相關配件的線上交易。這個平台提供了使用者註冊、登入、瀏覽、發布、編輯和刪除商品等功能，並使用 JWT 進行安全認證。本專案使用 Maven 作為構建工具，並使用 Maven Wrapper 簡化構建流程。前端部分使用 Vue.js 構建，並通過 Vite 進行開發和構建。
https://camera-market-cloud.web.app  
目前後端服務部署在 Google Cloud Run，前端服務部署在 Google Firebase Hosting，資料庫使用GCP的託管服務Cloud SQL，預計搬到 GCP 的 VM Compute Engine 裡面放一個DB，因為費用相對便宜很多，但在搬家完成之前資料庫會先關著省錢。@@

## 主要功能

*   **使用者管理：**
    *   **註冊：** 使用者可以透過提供使用者名稱、密碼和電子郵件來註冊帳戶。
    *   **登入：** 使用者可以使用其使用者名稱和密碼登入平台，並獲取 JWT Token。
    *   **個人資料：** 使用者可以查看自己的資訊。
*   **商品管理：**
    *   **瀏覽商品：** 使用者可以瀏覽平台上的所有商品。
    *   **發布商品：** 已登入的使用者可以發布新的商品，並記錄建立者和最後修改者。
    * **更新商品**: 使用者可以更新商品資訊。
*   **安全機制：**
    *   **JWT (JSON Web Tokens) 認證：** 使用 JWT 來確保 API 的安全存取。
    *   **密碼加密：** 使用 `BCryptPasswordEncoder` 來儲存加密過的密碼。
    * **CORS**: 允許前端 `http://localhost:5173` 跨域請求。
* **審計機制**: 使用JPA審計機制，自動記錄建立者和最後修改者。
* **測試**: 使用 Spring Boot Test, MockMvc 等工具進行測試。

## 技術棧

*   **後端：**
    *   Spring Boot
    *   Spring Security
    *   Spring Data JPA
    *   JWT (JSON Web Tokens)
    *   Maven
    *   BCryptPasswordEncoder
* **測試**:
    * Spring Boot Test
    * MockMvc
*   **前端：**
    *   Vue.js 3
    *   Vue Router
    *   Vite

## 專案結構說明

以下是專案主要檔案和目錄的說明：

*   **`.mvn/`**
    *   **`wrapper/maven-wrapper.jar`：** Maven Wrapper 的核心 JAR 檔案。
    *   **`wrapper/maven-wrapper.properties`：** Maven Wrapper 的設定檔，指定要使用的 Maven 版本和下載位置。
*   **`mvnw`**： (Unix/Linux/macOS) Maven Wrapper 的可執行腳本。
*   **`mvnw.cmd`**： (Windows) Maven Wrapper 的批次腳本。
*   **`pom.xml`**： Maven 的專案描述檔，定義專案的依賴、構建流程等。
*   **`src/main/java/jiamingla/first/camera/market/`**：專案的核心程式碼目錄。
    *   **`Application.java`**：Spring Boot 應用程式的主類別，用於啟動應用程式。
    *   **`controller/`**：存放處理 HTTP 請求的控制器類別。
        *   **`MemberController.java`**: 處理所有與會員相關的請求，例如會員註冊，登入，取得會員資訊。
        *   **`ListingController.java`**: 處理所有與商品列表相關的請求，例如取得商品列表。
    *   **`entity/`**：存放對應資料庫表格的實體類別。
        *   **`Member.java`**: 代表使用者的實體類別，包含使用者資訊，例如 `id`、`username`、`password`、`email`。
        *   **`Listing.java`**: 代表商品列表的實體類別，包含商品資訊，例如 `id`、`title`、`description`、`make`、`model`、`price`、`category`、`seller`。
    *   **`repository/`**：存放資料庫操作介面 (Repository)。
        *   **`MemberRepository.java`**: 提供操作 `Member` 實體類別的資料庫操作方法。
        *   **`ListingRepository.java`**: 提供操作 `Listing` 實體類別的資料庫操作方法。
    *   **`security/`**：存放 Spring Security 相關設定類別。
        *   **`SecurityConfig.java`**: 設定 Spring Security 的相關設定，例如設定那些路徑需要保護，使用那些 filter 等等。
    *   **`config/`**: 存放專案的設定類別。
        *   **`WebConfig.java`**: 用來設定跨域資源共享(CORS)相關的設定。
        *   **`AppConfig.java`**: 用來設定password encoder。
    *   **`dto/`**: 存放資料傳輸類別 (Data Transfer Object)。
        *   **`LoginRequest.java`**: 定義登入 API 的請求格式。
        *   **`RegisterRequest.java`**: 定義註冊 API 的請求格式。
        *   **`LoginResponse.java`**: 定義登入 API 的回應格式。
        *   **`MemberResponse.java`**: 定義取得會員資訊的 API 的回應格式。
    *   **`service/`**: 存放專案的業務邏輯類別。
        *   **`MemberService.java`**: 用來處理與 member 相關的業務邏輯，例如使用者登入，註冊，取得會員資訊。
*   **`src/main/resources/`**：存放應用程式的資源檔案。
    *   **`application.properties`**：應用程式的設定檔案，例如資料庫連線資訊、端口設定等。
    *   **`static/`**：存放靜態資源，例如 HTML、CSS、JavaScript 等。
    *   **`templates/`**：存放視圖模板檔案。
* **`src/test/java/jiamingla/first/camera/market/`**: 存放專案的測試程式碼。
  * **`LoginTest.java`**: 測試使用者登入以及相關安全性的測試類別。
  * **`ListingTest.java`**: 測試商品列表以及相關安全性的測試類別。

## 使用方法

### 前置準備

1.  **Java 開發環境：** 確保已安裝 JDK (Java Development Kit) 17 或以上版本。
2.  **Maven：** 雖然可以使用 Maven Wrapper，但建議也安裝 Maven，方便執行其他 Maven 命令。
3.  **資料庫:** 需要設置資料庫，並且在 `application.properties` 檔案中設定資料庫的連線資訊。
4. **Node.js**: 需要安裝 Node.js，才能執行前端程式碼。

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
    或者你可以直接執行 `Application.java` 這個類別，裡面有一個 main 方法，可以直接執行。
4. **使用 Maven Daemon(mvnd)**:
    1. 更改`.mvn/wrapper/maven-wrapper.properties` 中 `distributionUrl` 的值為 `maven-mvnd-*` 開頭的網址，例如：`distributionUrl=https\://repo.maven.apache.org/maven2/org/apache/maven/mvnd/maven-mvnd-0.9.0-m2/maven-mvnd-0.9.0-m2-bin.zip`
    2. 使用 `mvnw clean install` 或 `mvnw spring-boot:run` 即可使用 mvnd 來加速構建。
5. **執行前端程式碼**:
    1. 切換到 `frontend/camera-market` 目錄。
    2. 執行 `npm install` 來安裝依賴。
    3. 執行 `npm run dev` 來啟動前端程式碼。

## API 端點

*   **會員相關**
    *   `POST /api/members/register`：註冊新使用者。
        *   請求格式：
            ```json
            {
              "username": "testuser",
              "password": "password123",
              "email": "test@example.com"
            }
            ```
        *   回應：
            *   200 OK：註冊成功。
            *   400 Bad Request：輸入無效或使用者已存在。
    *   `POST /api/members/login`：使用者登入。
        *   請求格式：
            ```json
            {
              "username": "testuser",
              "password": "password123"
            }
            ```
        *   回應：
            *   200 OK：登入成功，返回 JWT Token。
            *   401 Unauthorized：使用者名稱或密碼錯誤。
* **商品相關**
    * `GET /api/listings`: 取得所有商品列表。
    * `POST /api/listings`: 新增商品。
    * `PATCH /api/listings/{id}`: 更新商品。

## 測試

* **`LoginTest.java`**: 測試使用者登入以及相關安全性的測試類別。
* **`ListingTest.java`**: 測試商品列表以及相關安全性的測試類別。
* **前端測試**:
    * **Vitest**: 使用 vitest 進行單元測試。
    * **Playwright**: 使用 playwright 進行 E2E 測試。

## 開發規劃

*   專案上CI，Branch merge前要過test
*   完善商品管理功能，增加商品的圖片、規格等資訊。
*   增加搜尋和篩選商品的功能。
*   優化使用者介面和體驗。
*   增加後台管理功能。
* ...等等
=======

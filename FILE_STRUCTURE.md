
### 主要檔案和目錄說明

*   **`.mvn/`**
    *   **`wrapper/maven-wrapper.jar`：** Maven Wrapper 的核心 JAR 檔案。
    *   **`wrapper/maven-wrapper.properties`：** Maven Wrapper 的設定檔，指定要使用的 Maven 版本和下載位置。
*   **`mvnw`**： (Unix/Linux/macOS) Maven Wrapper 的可執行腳本。
*   **`mvnw.cmd`**： (Windows) Maven Wrapper 的批次腳本。
*   **`pom.xml`**： Maven 的專案描述檔，定義專案的依賴、構建流程等。
*   **`src/main/java/jiamingla/first/camera/market/`**：專案的核心程式碼目錄。
    *   **`Application.java`**：
        *   **用途：** Spring Boot 應用程式的主類別，包含 `main` 方法，用於啟動應用程式。
        *   **說明：** 這個類別使用 `@SpringBootApplication` 注解，表示它是一個 Spring Boot 應用程式的入口點。Spring Boot 會自動掃描這個類別所在的套件及其子套件，尋找 Spring 組件。
    *   **`controller/`**：
        *   **用途：** 存放處理 HTTP 請求的控制器類別。
        *   **說明：** 這些類別使用 `@RestController` 或 `@Controller` 注解，負責接收來自用戶端的請求，並返回相應的結果。它們通常會調用 `service` 層的類別來執行業務邏輯。
        * **示例檔案:**
          * **`MemberController.java`**: 處理所有與會員相關的請求，例如會員註冊，登入，取得會員資訊。
          * **`ListingController.java`**: 處理所有與商品列表相關的請求，例如取得商品列表。
    *   **`entity/`**：
        *   **用途：** 存放對應資料庫表格的實體類別。
        *   **說明：** 這些類別使用 `@Entity` 注解，表示它們是 JPA (Java Persistence API) 的實體類別，可以對應到資料庫中的表格。它們通常包含與資料庫表格欄位對應的屬性和 getter/setter 方法。
        *   **`Member.java`**:
            *   **用途：** 代表使用者的實體類別。
            *   **說明：** 包含使用者資訊，例如 `id`、`username`、`password`、`email`。使用 `@Entity`、`@Id`、`@GeneratedValue` 等 JPA 注解來定義實體和主鍵。
        *   **`Listing.java`**:
            *   **用途：** 代表商品列表的實體類別。
            *   **說明：** 包含商品資訊，例如 `id`、`title`、`description`、`make`、`model`、`price`、`category`、`seller`。使用 JPA 注解定義實體和屬性。
    *   **`repository/`**：
        *   **用途：** 存放資料庫操作介面 (Repository)。
        *   **說明：** 這些介面繼承自 `JpaRepository` 或 `CrudRepository`，提供了一系列預設的資料庫操作方法，例如查詢、新增、修改、刪除等。它們負責與資料庫進行互動。
        *   **`MemberRepository.java`**:
            *   **用途：** 提供操作 `Member` 實體類別的資料庫操作方法。
            *   **說明：** 繼承自 `JpaRepository<Member, Long>`，可以對 `Member` 實體進行資料庫操作，例如透過使用者名稱查詢會員、新增會員等。
        *   **`ListingRepository.java`**:
            *   **用途：** 提供操作 `Listing` 實體類別的資料庫操作方法。
            *   **說明：** 繼承自 `JpaRepository<Listing, Long>`，可以對 `Listing` 實體進行資料庫操作，例如查詢商品、新增商品等。
    *   **`security/`**：
        *   **用途：** 存放 Spring Security 相關設定類別。
        *   **說明：** 這個目錄可能包含自定義的過濾器、身份驗證提供者、權限設定等，用於配置 Spring Security。
        * **示例檔案:**
          * **`SecurityConfig.java`**: 設定 Spring Security 的相關設定，例如設定那些路徑需要保護，使用那些 filter 等等。
    *   **`config/`**:
        *   **用途**: 存放專案的設定類別。
        *   **說明**: 這個目錄通常用來存放設定檔相關類別。
        * **示例檔案:**
          * **`WebConfig.java`**: 用來設定跨域資源共享(CORS)相關的設定。
          * **`AppConfig.java`**: 用來設定password encoder。
    *   **`dto/`**:
        *   **用途**: 存放資料傳輸類別 (Data Transfer Object)。
        *   **說明**: 這些類別用來定義 API 請求和回應的資料格式，通常與 entity 類別不同，可以只包含部分 entity 類別的屬性。
        * **示例檔案:**
          * **`LoginRequest.java`**: 定義登入 API 的請求格式。
          * **`RegisterRequest.java`**: 定義註冊 API 的請求格式。
          * **`LoginResponse.java`**: 定義登入 API 的回應格式。
          * **`MemberResponse.java`**: 定義取得會員資訊的 API 的回應格式。
    *   **`service/`**:
        *   **用途**: 存放專案的業務邏輯類別。
        *   **說明**: 這些類別負責處理業務邏輯，例如使用者登入、註冊、管理商品等等。它們通常會調用 repository 層來進行資料庫操作。
         * **示例檔案:**
          * **`MemberService.java`**: 用來處理與 member 相關的業務邏輯，例如使用者登入，註冊，取得會員資訊。
*   **`src/main/resources/`**：存放應用程式的資源檔案。
    *   **`application.properties`**：應用程式的設定檔案，例如資料庫連線資訊、端口設定等。
    *   **`static/`**：存放靜態資源，例如 HTML、CSS、JavaScript 等。
    *   **`templates/`**：存放視圖模板檔案。
* **`src/test/java/jiamingla/first/camera/market/`**: 存放專案的測試程式碼。
  * **`LoginTest.java`**: 測試使用者登入以及相關安全性的測試類別。

### 測試類別說明 (`src/test/java/jiamingla/first/camera/market/LoginTest.java`)

*   **`LoginTest.java`** 是一個測試類別，用於驗證使用者登入和相關 API 的安全性。
*   **主要功能：**
    *   測試使用者成功登入。
    *   測試錯誤密碼或未知使用者登入失敗。
    *   測試未登入情況下存取受保護的端點。
    *   測試會員註冊API。
    * 測試取得會員資訊，並且可以取得其商品列表。
    * 測試未登入情況下取得會員資訊。
* **主要技術**
  * `@SpringBootTest`：表示這是一個 Spring Boot 整合測試。
  * `@AutoConfigureMockMvc`：自動配置 MockMvc，用於模擬 HTTP 請求。
  * `@Transactional`：表示每個測試方法都在一個事務中執行，測試結束後會回滾資料庫。
  * `@BeforeEach`：在每個測試方法執行之前執行的設定方法，用於建立測試環境。
  * `@Autowired`：注入 Spring 容器管理的物件，例如 MockMvc、PasswordEncoder、MemberRepository、ListingRepository、ObjectMapper。
  * `MockMvc`：用於模擬發送 HTTP 請求。
  * `ObjectMapper`：用於將 JSON 字串轉換為物件或將物件轉換為 JSON 字串。
  * `PasswordEncoder`: 用於加密密碼。
  * `MemberRepository`: 用於操作 Member 資料表。
  * `ListingRepository`: 用於操作 Listing 資料表。
  * `token`: 登入後取得的 jwt token。
  * `member`: 測試所用的 member 實體類別。

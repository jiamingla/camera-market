# 更新日誌 (Changelog)

## 2025/03/15

### 新增

-   **`Listing` 實體新增 `createdBy` 和 `lastModifiedBy` 欄位：** 用於記錄商品列表的建立者和最後修改者。
-   **新增 `@EntityListeners(AuditingEntityListener.class)` 到 `Listing` 實體:** 讓 `@CreatedBy` 和 `@LastModifiedBy` 可以正常運作。
- **新增 `AuditingEntityListener`**: 讓JPA知道我們需要啟用審計功能。
- **新增 `@CreatedBy` 和 `@LastModifiedBy`**: 讓 JPA 知道我們要使用這兩個標註。
-   **新增 `SpringSecurityAuditorAware` 類別：** 用於取得當前登入的使用者名稱，以供 JPA 審計使用。
-   **`Application.java` 新增 `@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")` 註解：** 啟用 JPA 審計並指定使用 `SpringSecurityAuditorAware`。
-   **`ListingService.java` 所有方法新增 `@Transactional` 註解：** 確保每個資料庫操作都在正確的認證環境中執行。
-   **`Category` 列舉類別：** 用於表示商品類別，提升資料的正確性。
- **`Make` 列舉類別:** 用於表示商品廠牌，提升資料的正確性。
- **`ListingTest`更新:**
    - 檢查`createdBy`和`lastModifiedBy`的數值。
    - 使用`isNotEmpty()`來檢查時間欄位是否有數值，而不是直接比較時間。
    - 新增 `testCreateListingWithWrongCategory` 和 `testUpdateListingWithWrongCategory` 測試。
    - 手動設定測試時的 `SecurityContext`。
    - 重構 `BeforeEach`，使其更易讀。
- **`LoginTest`更新:**
    - 移除不必要的 `listingRepository`。
- 新增 `.mvn` 到 `.gitignore`
- 在 `README.md` 新增測試說明。
- 在 `README.md` 新增 `mvnd` 說明。

### 修改

-   **`ListingController` 的 `updateListing` 方法：** 從 `PUT` 改為 `PATCH`，更符合部分更新的語義。
-   **`Listing` 實體的 `make` 欄位：** 從 `String` 改為 `Make` 列舉類別，提升資料的正確性。
- **`Listing` 實體的 `category` 欄位:** 從 `String` 改為 `Category` 列舉類別，提升資料的正確性。
-   **`SpringSecurityAuditorAware` 類別：** 修改為可以正確處理 `UserDetails` 物件的狀況。
- **`ListingService`的`getAllListings`方法:** 使用 `lastUpdateTime` 降序排序。
- **`createTime` 和 `lastUpdateTime` 的產生方式：** 使用 `@CreatedDate` 和 `@LastModifiedDate` 註解自動產生。
- **重構日誌:** 添加更多日誌來方便debug。
- **`MemberController`:** 在`registerMember`方法使用`Member`取代`MemberResponse`。
- **`MemberController`:** 更新log訊息，從 `Returning Member With Listings, member: {}` 改為 `Returning Member With Listings, member username: {}`。

### 移除

-   **`UpdateListingDTO` 類別：** 不再需要，已被 `Listing` 實體取代。
- **`CreateListingDTO` 類別:** 不再需要，已被 `Listing` 實體取代。
-   **`Listing` 實體中的 `@EntityListeners(SpringSecurityAuditorAware.class)`：** 錯誤的使用方式，已移除。
-  **移除 `Application.java` 中不必要的 `@EnableWebSecurity`**

### 修復

-   **`createTime` 和 `lastUpdateTime` 為 `null` 的問題：** 已修正商品列表建立和更新時間戳記無法自動填入的問題。
-   **單元測試中 Spring Security 認證環境設定錯誤的問題：** 已修正單元測試中無法正確設定 Spring Security 環境，導致審計欄位為空的問題。
- **測試無法通過:** 修復了單元測試無法通過的問題。
- **JPA 審計機制:** 修復JPA審計機制無法正常運作。
- **JPA 審計機制:** 修復了JPA審計機制的誤解。
- 修復 `getName()` 回傳 `null` 的問題。
- 更新文件：更新 `FILE_STRUCTURE.md` 和 `README.md`
- 修復在刪除或更新商品時，找不到會員的問題。

## 2025/03/14

[https://github.com/jiamingla/camera-market/pull/2](https://github.com/jiamingla/camera-market/pull/2)
[https://github.com/jiamingla/camera-market/issues/3](https://github.com/jiamingla/camera-market/issues/3)

-   新增了日誌紀錄 (log)。
-   理解了 ORM 對於物件操作的寫法 -> 記錄在 issue 裡了。
-   將商品廠牌 (`Make`) 從字串改為枚舉。
-   將 `ListingController` 的更新方法從 `PUT` 改為 `PATCH` -> 因為通常不會有人整份商品都要更新。

在你的 Camera Market 專案中，前端（Vue.js）和後端（Spring Boot）之間通過 API 進行資料交換，而資料交換的格式是 JSON。為了讓前端和後端能夠正確地理解彼此傳送的資料，我們需要遵循一些約定，其中一個重要的約定就是 JSON 鍵值的大小寫。

1. JSON 鍵值大小寫的約定：

推薦使用小寫： 在 JSON 中，我們強烈推薦使用小寫字母來命名鍵值。例如，"title"、"description"、"make" 等。這是 JSON 世界中最常見的約定，可以讓你的 API 更具一致性和可讀性。
後端 camelCase： 在你的 Spring Boot 後端，Java 實體類別（例如 Listing.java）的欄位通常使用 camelCase 命名法，也就是駝峰式命名法（例如 title、description、make、model）。
Jackson 的靈活性： Spring Boot 使用 Jackson 函式庫來處理 JSON 的序列化和反序列化。Jackson 非常靈活，即使 JSON 鍵值的大小寫與 Java 欄位的大小寫不完全一致，它也能夠在大多數情況下正確地進行映射。但是，為了程式碼的清晰和一致性，我們仍然建議遵循小寫的 JSON 鍵值約定。
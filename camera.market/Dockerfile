# 使用 JDK 17 作為基底映像
FROM eclipse-temurin:17-jdk
# 設定工作目錄
RUN ls -aR
# 拷貝 jar 檔進去容器
COPY target/*.jar app.jar

# 啟動服務
ENTRYPOINT ["java", "-jar", "/app.jar"]

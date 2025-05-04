package jiamingla.first.camera.market.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * 主數據源配置 (用於 application.properties)
     * 會自動讀取 spring.datasource.* 相關屬性
     */
    @Bean
    @Primary // 標記為主 DataSourceProperties
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 測試環境數據源配置 (用於 application-test.properties)
     * 會自動讀取 spring.datasource.* 相關屬性 (但會被 test profile 的文件覆蓋)
     */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari") // 讀取 Hikari 特定配置
    @Profile("test") // 只在 'test' profile 激活時創建這個 DataSource Bean
    public DataSource testDataSource(DataSourceProperties properties) {
        // 使用從 application-test.properties 加載的 H2 配置創建 HikariDataSource
        // 這裡不會讀取或使用 socketFactory/cloudSqlInstance 屬性
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                                            .type(HikariDataSource.class).build();
        // 可以選擇性地設置 H2 特定的連接池參數
        // dataSource.setMaximumPoolSize(5);
        System.out.println("--- Using H2 DataSource for Testing ---");
        return dataSource;
    }

    /**
     * 生產/開發環境數據源配置 (Cloud SQL)
     * 會自動讀取 spring.datasource.* 和 spring.datasource.hikari.* 相關屬性
     */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari") // 讀取 Hikari 特定配置
    @Profile("!test") // 在非 'test' profile 激活時創建這個 DataSource Bean
    public DataSource cloudSqlDataSource(DataSourceProperties properties) {
        // 使用從 application.properties 加載的 Cloud SQL 配置創建 HikariDataSource
        // 這裡會讀取並使用 socketFactory 和 cloudSqlInstance 屬性
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                                            .type(HikariDataSource.class).build();
        // 可以選擇性地設置 Cloud SQL 特定的連接池參數
        // dataSource.setMaximumPoolSize(10);
        System.out.println("--- Using Cloud SQL DataSource ---");
        return dataSource;
    }
}

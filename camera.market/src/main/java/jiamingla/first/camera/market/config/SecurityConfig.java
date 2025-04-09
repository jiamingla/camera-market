package jiamingla.first.camera.market.config;

import jiamingla.first.camera.market.service.CustomUserDetailsService;
import jiamingla.first.camera.market.util.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF 保護
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 添加 CORS 配置
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new BasicAuthenticationEntryPoint())) // 添加這一行
                //設定API的權限, 先通過permitAll, 再通過authenticated, 最後才檢查JwtRequestFilter
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/members/register", "/api/members/login").permitAll() // 允許註冊和登錄接口不需要token
                        .requestMatchers(HttpMethod.GET, "/api/listings/**").permitAll() // 允許 GET /api/listings/** 不需要 token
                        .anyRequest().authenticated() // 其他所有請求都需要通過驗證
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// 使用 JWT，不需要 Session
                //把JwtRequestFilter加到UsernamePasswordAuthenticationFilter的前面
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }

    // 配置 CORS
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173", // 保留本地開發環境
                "https://camera-market-frontend-cloud.web.app"  // 你的 Firebase Hosting 正式網址
                // TODO: 如果你有自訂網域，也加進來，例如: "https://your-custom-domain.com"
        ));
        // 允許的 HTTP 方法 (可以保持原樣或根據需要調整)
        // 確保包含前端會用到的所有方法，特別是 OPTIONS (瀏覽器會發送 Preflight 請求)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 允許所有標頭
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 允許跨域請求攜帶憑證（例如 cookies）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // 可以更精確地只對 /api 路徑生效
        return source;
    }
}

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
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    // 配置 CORS
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 允許來自 http://localhost:5173 的請求
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允許的 HTTP 方法
        configuration.setAllowedHeaders(Arrays.asList("*")); // 允許所有標頭
        configuration.setAllowCredentials(true); // 允許跨域請求攜帶憑證（例如 cookies）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 對所有路徑應用此 CORS 配置
        return source;
    }
}

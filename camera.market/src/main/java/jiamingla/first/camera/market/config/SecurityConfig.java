package jiamingla.first.camera.market.config;

import jiamingla.first.camera.market.service.CustomUserDetailsService;
import jiamingla.first.camera.market.util.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // 導入 WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // --- 新增 WebSecurityCustomizer Bean ---
    // @Bean
    // @Profile("!test") // 表示當 'test' profile *不*活躍時，才創建這個 Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    //     // 配置 WebSecurity 忽略所有 OPTIONS 請求
    //     // 這將使 OPTIONS 請求完全繞過 Spring Security Filter Chain
    //     // 允許 CorsFilter 更早地處理它們
    //     return (web) -> web.ignoring().requestMatchers(HttpMethod.OPTIONS, "/**");
    // }
    // --- WebSecurityCustomizer Bean 結束 ---

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(new BasicAuthenticationEntryPoint()))
                .authorizeHttpRequests(authz -> authz
                        // --- 從這裡移除 OPTIONS 的 permitAll，因為 WebSecurityCustomizer 會處理 ---
                        // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // --- 保持現有規則 ---
                        // 建議明確指定 POST 方法
                        .requestMatchers(HttpMethod.POST, "/api/members/register", "/api/members/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/member/**").permitAll() // 使用 ** 匹配路徑參數
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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

    // 配置 CORS (保持不變)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://camera-market-frontend-cloud.web.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}

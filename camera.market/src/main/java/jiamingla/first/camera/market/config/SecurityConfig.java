package jiamingla.first.camera.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @SuppressWarnings({ "removal", "deprecation" })
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for simplicity (consider enabling in production)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/members/register").permitAll() // Allow public access to registration
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .httpBasic(org.springframework.security.config.Customizer.withDefaults()); // Use basic authentication (for simplicity)
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


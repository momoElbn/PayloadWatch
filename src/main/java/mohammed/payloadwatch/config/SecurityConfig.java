package mohammed.payloadwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Enable CORS
                .cors(Customizer.withDefaults())

                // 2. Disable CSRF (Not needed for stateless JWT APIs)
                .csrf(csrf -> csrf.disable())

                // 3. Secure the endpoints
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to load the static HTML/JS/CSS frontend files
                        .requestMatchers("/", "/index.html", "/login.html", "/js/**", "/css/**").permitAll()
                        // Require a valid Cognito JWT for anything under /api/
                        .requestMatchers("/api/**").authenticated()
                        // Allow everything else
                        .anyRequest().permitAll()
                )

                // 4. Tell Spring to act as an OAuth2 Resource Server and expect JWTs
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    // CORS Configuration: Allows your frontend to talk to your API endpoints
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow common local development ports
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

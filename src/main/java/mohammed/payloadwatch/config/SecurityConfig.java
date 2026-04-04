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
                // enable cors
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // disable csrf
                .csrf(csrf -> csrf.disable())

                // configure endpoints
                .authorizeHttpRequests(authz -> authz
                        // public static files
                        .requestMatchers("/", "/index.html", "/login.html", "/js/**", "/css/**").permitAll()
                        // health check endpoint
                        .requestMatchers("/api/health").permitAll()
                        // public api config
                        .requestMatchers("/api/public/**").permitAll()
                        // secure endpoints
                        .requestMatchers("/api/**").authenticated()
                        // default policy
                        .anyRequest().permitAll()
                )

                // parse jwt
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // configure cors
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // local dev patterns
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

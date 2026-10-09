package app.jobzy.api.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The security filter chain. For now it secures nothing on purpose (ADR 0003): every request is
 * permitted, so adding Spring Security does not change behaviour before login exists. Having the
 * chain in place means the login slice only tightens this configuration, and tests already run
 * through the security filters.
 *
 * <p>CSRF protection, form login, HTTP basic and logout are disabled because the API is stateless
 * and will authenticate with bearer tokens, not cookies or sessions. CORS defers to the Spring MVC
 * configuration in {@link WebConfig}.
 */
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
        .build();
  }
}

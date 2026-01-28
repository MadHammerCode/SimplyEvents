package at.fhv.simplyevents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/dashboard",
                    "/event-details/**",
                    "/register",
                    "/register/vendor",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                        "/uploads/**",
                    "/webjars/**"
                ).permitAll()

                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/events/backoffice**").hasAnyAuthority("BACKOFFICE", "ADMIN")
                .requestMatchers("/api/events/**").permitAll()

                .requestMatchers("/admin-dashboard").hasAuthority("ADMIN")

                .requestMatchers("/backoffice-dashboard/**", "/event-editor/**", "/create-event/**", "/edit-event/**")
                .hasAnyAuthority("BACKOFFICE", "ADMIN")

                .requestMatchers("/invoices/**")
                .hasAnyAuthority("FRONTOFFICE", "BACKOFFICE", "ADMIN")

                .requestMatchers("/frontoffice-checkin/**")
                .hasAnyAuthority("FRONTOFFICE", "ADMIN")

                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> {
                form.loginPage("/login");
                form.defaultSuccessUrl("/dashboard", true); // Default landing page
                form.permitAll();

            })
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/dashboard")
                    .logoutRequestMatcher(org.springframework.security.web.util.matcher.RegexRequestMatcher.regexMatcher(org.springframework.http.HttpMethod.GET, "/logout"))
                    .permitAll())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


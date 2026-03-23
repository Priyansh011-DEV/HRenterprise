package HR.enterprise.config;

import HR.enterprise.security.JwtFilter;
import HR.enterprise.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // 🔓 Public endpoints
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        // 🔓 STATIC FILES (🔥 THIS WAS MISSING)
                        .requestMatchers(
                                "/login.html",
                                "/dashboard.html",
                                "/employee.html",
                                "/register.html",
                                "/app.js",
                                "/style.css"
                        ).permitAll()

                        //Permit ALL files
                        .requestMatchers("/*.css", "/*.js", "/*.html", "/static/**").permitAll()

                        // 🔐 Employee module
                        .requestMatchers(HttpMethod.POST, "/employees").hasAnyRole("ADMIN","HR")
                        .requestMatchers(HttpMethod.GET, "/employees/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/employees/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/employees/**").hasRole("ADMIN")

                        // 🏖️ Leave module
                        .requestMatchers(HttpMethod.POST, "/api/leaves/apply").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/leaves/my").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/leaves/all").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/api/leaves/**").hasAnyRole("ADMIN", "HR")

                        // 🔒 Everything else
                        .anyRequest().authenticated()
                )

                .userDetailsService(userDetailsService)

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
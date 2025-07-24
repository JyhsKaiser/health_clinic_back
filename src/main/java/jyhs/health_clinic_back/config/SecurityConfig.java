package jyhs.health_clinic_back.config;

// Importaciones de Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Importaciones de Lombok (si estás usando Lombok)
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import jyhs.health_clinic_back.service.AuthService;

/**
 * Clase de configuración principal para la seguridad de la aplicación Spring.
 * Esta clase define las reglas de seguridad a nivel de HTTP, la gestión de sesiones,
 * la integración del filtro JWT y la configuración de los proveedores de autenticación.
 */
@Configuration      // Anotación: Indica que esta clase es una fuente de definiciones de beans
// para el contexto de la aplicación de Spring. Spring escaneará esta clase
// para encontrar métodos anotados con @Bean y registrará los objetos que retornan
// como beans gestionados por el contenedor.
@EnableWebSecurity  // Anotación: Habilita la integración de seguridad web de Spring.
// Esto importa la configuración de Spring Security en la aplicación,
// permitiendo definir reglas de seguridad HTTP, autenticación, autorización, etc.
@RequiredArgsConstructor    // Anotación de Lombok: Genera automáticamente un constructor con
// argumentos para todos los campos 'final' no inicializados.
// En este caso, inyecta 'jwtFilter' y 'authenticationProvider'.
// Esto evita tener que escribir el constructor manualmente para la inyección de dependencias.
@EnableMethodSecurity   // Anotación: Habilita la seguridad a nivel de método utilizando anotaciones como
// @PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter. Permite aplicar reglas
// de seguridad directamente en los métodos de los servicios o controladores.
public class SecurityConfig {

    // Inyección de dependencia del filtro JWT personalizado.
    // Este filtro será responsable de interceptar las peticiones para validar los tokens JWT.
    private final JwtFilter jwtFilter;
    // Inyección de dependencia del proveedor de autenticación.
    // Este proveedor es responsable de autenticar a los usuarios (ej. mediante nombre de usuario y contraseña).
    private final AuthenticationProvider authenticationProvider;

    private AuthService authService;

    @Bean
    public String[] publicEndpoints(){
        return new String[] {
                "/api/v1/auth/**",
//                "/api/todos/**"

        };
    }
    /**
     * Configura la cadena de filtros de seguridad HTTP para la aplicación.
     * Este método define las reglas de autorización, la gestión de sesiones y la
     * integración de filtros personalizados en el flujo de seguridad.
     *
     * @param httpSecurity Objeto HttpSecurity que permite configurar la seguridad a nivel HTTP.
     * @return Una instancia de {@link SecurityFilterChain} que define la cadena de filtros de seguridad.
     * @throws Exception Si ocurre un error durante la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain segurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints()).permitAll()
                        .anyRequest().authenticated()
                )
//                .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
//                        .addLogoutHandler())  // La URL del logout
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }



}
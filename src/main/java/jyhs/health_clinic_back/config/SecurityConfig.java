package jyhs.health_clinic_back.config;

// Importaciones de Spring Security

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer; // ¡Nueva importación necesaria!
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // ¡Nueva importación necesaria!
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Importaciones para CORS ¡Nuevas importaciones necesarias!
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Importaciones de Lombok (si estás usando Lombok)
import lombok.RequiredArgsConstructor;

import java.util.Arrays; // ¡Nueva importación necesaria para Arrays.asList!

/**
 * Clase de configuración principal para la seguridad de la aplicación Spring.
 * Esta clase define las reglas de seguridad a nivel de HTTP, la gestión de sesiones,
 * la integración del filtro JWT y la configuración de los proveedores de autenticación.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    // El AuthService no es necesario aquí a menos que lo uses directamente en este bean.
    // private AuthService authService;

    @Bean
    public String[] publicEndpoints() {
        return new String[]{
                "/api/v1/auth/**",
//                "/api/v1/patient/**",
                // Agrega aquí cualquier otra ruta pública que no requiera autenticación
                // Por ejemplo, si tienes una ruta para verificar el estado de la API:
                // "/api/status/**"
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
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // Deshabilita CSRF (Cross-Site Request Forgery). Es común deshabilitarlo en APIs REST
                // que usan JWT porque los JWTs son stateless y no dependen de cookies de sesión.
                .csrf(AbstractHttpConfigurer::disable)
                // Habilita CORS. Spring Security buscará un bean de CorsConfigurationSource
                // para aplicar las reglas CORS.
                .cors(Customizer.withDefaults()) // ¡Aquí se habilita CORS!
                .authorizeHttpRequests(auth -> auth
                        // Permite el acceso sin autenticación a los endpoints definidos como públicos.
                        .requestMatchers(publicEndpoints()).permitAll()
                        // Cualquier otra solicitud (que no sea pública) debe estar autenticada.
                        .anyRequest().authenticated()
                )
                // Configura la gestión de sesiones como STATELESS (sin estado).
                // Esto es fundamental para las APIs REST que usan JWT, ya que cada solicitud
                // debe llevar su propio token de autenticación y no se mantienen sesiones en el servidor.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Establece el proveedor de autenticación que se utilizará para verificar las credenciales del usuario.
                .authenticationProvider(authenticationProvider)
                // Añade tu filtro JWT personalizado antes del filtro de autenticación de usuario/contraseña
                // estándar de Spring Security. Esto asegura que el JWT se valide primero.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * Define la configuración de CORS para la aplicación.
     * Este bean es crucial para permitir que tu frontend (ej. React Native, React Web)
     * pueda hacer solicitudes a tu backend desde un origen diferente.
     *
     * @return Una instancia de CorsConfigurationSource que contiene las reglas CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // ¡IMPORTANTE! Aquí debes listar los orígenes exactos de tus frontends.
        // - "http://localhost:5173": Si tu aplicación web React/Vue/Angular se ejecuta en este puerto.
        // - "http://localhost:3000": Otro puerto común para desarrollo web.
        // - "exp://192.168.1.5:19000": Si usas React Native con Expo Go, esta URL puede variar
        //   según tu IP local. Debes verificarla en tu terminal de Expo.
        // - "https://tu-dominio-de-produccion.com": El dominio de tu frontend en producción.
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000"
                // "exp://192.168.1.5:19000", // Descomenta y ajusta si usas Expo Go
                // "https://tu-dominio-de-produccion.com" // Descomenta y ajusta para producción
        ));
        // Define los métodos HTTP que están permitidos para las solicitudes de origen cruzado.
        // "OPTIONS" es esencial para las solicitudes preflight de CORS.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Define las cabeceras que están permitidas en las solicitudes de origen cruzado.
        // "*" permite todas las cabeceras, pero puedes ser más específico si lo necesitas.
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Indica si las solicitudes pueden incluir credenciales (cookies, encabezados de autorización).
        // Si estableces esto a 'true', NO puedes usar "*" en allowedOrigins; debes especificar orígenes exactos.
        configuration.setAllowCredentials(true);
        // Define el tiempo máximo (en segundos) que los resultados de una solicitud preflight (OPTIONS)
        // pueden ser cacheados por el cliente. Esto reduce el número de solicitudes OPTIONS.
        configuration.setMaxAge(3600L); // 1 hora de caché

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración CORS a todas las rutas ("/**") de tu API.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

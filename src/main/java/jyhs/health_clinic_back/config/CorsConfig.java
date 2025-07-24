package jyhs.health_clinic_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase de configuración para CORS (Cross-Origin Resource Sharing).
 * Define las reglas que permiten a tu backend aceptar solicitudes de orígenes cruzados,
 * lo cual es esencial para que tu aplicación React Native pueda comunicarse con él.
 */
@Configuration // Indica que esta clase es una fuente de definiciones de beans para el contexto de la aplicación.
public class CorsConfig {

    /**
     * Define un bean de WebMvcConfigurer para configurar las reglas CORS.
     * Este bean permite personalizar la configuración de Spring MVC, incluyendo el registro de CORS.
     *
     * @return Una implementación anónima de WebMvcConfigurer que configura las reglas CORS.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Añade las asignaciones de CORS a la configuración de la aplicación.
             * Aquí se especifican los orígenes permitidos, los métodos HTTP y las cabeceras.
             *
             * @param registry El registro de CORS al que se añadirán las configuraciones.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Configura las reglas CORS para todas las rutas ("/**") en tu API.
                registry.addMapping("/**")
                        // Permite solicitudes de cualquier origen ("*").
                        // En producción, es recomendable reemplazar "*" con los orígenes específicos de tu frontend
                        // (ej. "http://localhost:3000", "exp://192.168.1.5:19000", "https://tudominio.com").
                        .allowedOrigins("*") // Permite todos los orígenes
                        // Permite los métodos HTTP comunes para APIs REST.
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Permite todas las cabeceras en las solicitudes.
                        .allowedHeaders("*")
                        // Indica si las solicitudes deben incluir credenciales (cookies, encabezados de autorización HTTP).
                        // Si estableces allowedOrigins a "*", no puedes usar allowCredentials(true).
                        // Para permitir credenciales, debes especificar orígenes exactos.
                        // .allowCredentials(true) // Descomentar y usar con orígenes específicos si es necesario
                        // Define el tiempo máximo (en segundos) que los resultados de una solicitud preflight (OPTIONS)
                        // pueden ser cacheados por el cliente.
                        .maxAge(3600); // 1 hora de caché para las solicitudes preflight
            }
        };
    }
}

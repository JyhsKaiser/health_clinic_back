package jyhs.health_clinic_back.config;



import jyhs.health_clinic_back.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jyhs.health_clinic_back.repository.PatientRepository;

/**
 * Clase de configuración de la aplicación.
 * Esta clase define y expone beans importantes para el contexto de Spring,
 * particularmente para la configuración de seguridad.
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    // Inyección de dependencia del UserRepository
    // Lombok se encarga de generar el constructor para esta inyección.
    private final PatientRepository patientRepository;

    /**
     * Define un bean {@link AuthenticationProvider} que utiliza un enfoque basado en DAO (Data Access Object).
     * Este proveedor es fundamental para el proceso de autenticación en Spring Security,
     * configurando cómo se recuperarán los detalles del usuario y cómo se verificarán las contraseñas.
     *
     //     * @param userDetailsService El servicio para cargar los detalles del usuario. Spring inyectará este bean.
     //     * @param passwordEncoder    El codificador de contraseñas para verificar las credenciales. Spring inyectará este bean.
     * @return Una instancia de {@link DaoAuthenticationProvider} configurada.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
//            UserDetailsService userDetailsService // Spring inyectará tu bean userDetailsService aquí
//            PasswordEncoder passwordEncoder // Spring inyectará tu bean passwordEncoder aquí
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); // Se inyecta el servicio de usuario
        authProvider.setPasswordEncoder(passwordEncoder());       // Se inyecta el codificador de contraseñas
        return authProvider;
    }

    /**
     * Define un bean {@link UserDetailsService} personalizado.
     * Este bean es fundamental para el proceso de autenticación de Spring Security,
     * ya que es responsable de cargar los detalles del usuario a partir de su nombre de usuario (en este caso, su username).
     *
     * @return Una implementación de {@link UserDetailsService} que busca usuarios por su username.
     * @throws UsernameNotFoundException Si el usuario no es encontrado en la base de datos,
     *                                   se lanza esta excepción para indicar un fallo en la autenticación.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Retorna una implementación lambda de UserDetailsService.
        // Spring Security la utilizará para obtener los detalles de un usuario
        // durante el proceso de autenticación, típicamente cuando se intenta iniciar sesión.
        return email -> patientRepository
                .findByEmail(email) // Busca un usuario en la base de datos por su username.
                .orElseThrow(() -> new UsernameNotFoundException("User not found")); // Si no se encuentra, lanza una excepción.
    }

    /**
     * Define un bean {@link PasswordEncoder} que utiliza el algoritmo BCrypt.
     * Este encoder es esencial para la seguridad de las contraseñas, ya que las codifica
     * de forma segura antes de almacenarlas y las verifica durante el inicio de sesión.
     * BCrypt es un algoritmo de hashing de contraseñas robusto y recomendado.
     *
     * @return Una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Retorna una nueva instancia de BCryptPasswordEncoder, que es una implementación
        // popular y segura para el hashing de contraseñas.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


}

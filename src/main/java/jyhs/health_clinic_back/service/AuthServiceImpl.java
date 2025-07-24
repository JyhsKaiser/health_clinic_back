package jyhs.health_clinic_back.service;

import jyhs.health_clinic_back.entity.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jyhs.health_clinic_back.config.JwtService;
import jyhs.health_clinic_back.controller.models.AuthResponse;
import jyhs.health_clinic_back.controller.models.AuthenticationRequest;
import jyhs.health_clinic_back.controller.models.RegisterRequest;
import jyhs.health_clinic_back.entity.Role;
import jyhs.health_clinic_back.repository.PatientRepository;

/**
 * Implementación del servicio de autenticación {@link AuthService}.
 * Esta clase maneja la lógica de negocio para el registro de nuevos usuarios
 * y la autenticación de usuarios existentes, generando tokens JWT para sesiones seguras.
 */
@Service    // Anotación: Marca esta clase como un "Service" en la capa de servicios de Spring.
// Spring detectará esta clase durante el escaneo de componentes y la registrará
// como un bean, haciéndola elegible para la inyección de dependencias.
@RequiredArgsConstructor    // Anotación de Lombok: Genera automáticamente un constructor con
// argumentos para todos los campos 'final' no inicializados.
// Esto facilita la inyección de las dependencias requeridas
// (userRepository, passwordEncoder, jwtService, authenticationManager).
public class AuthServiceImpl implements AuthService {

    // Inyección de dependencia: Repositorio para interactuar con la base de datos y gestionar usuarios.
    private final PatientRepository patientRepository;
    // Inyección de dependencia: Codificador de contraseñas, utilizado para hashear las contraseñas
    // de los usuarios antes de almacenarlas, garantizando su seguridad.
    private final PasswordEncoder passwordEncoder;
    // Inyección de dependencia: Servicio JWT, responsable de generar y validar tokens JWT.
    private final JwtService jwtService;
    // Inyección de dependencia: Administrador de autenticación de Spring Security.
    // Es el punto de entrada para el proceso de autenticación de usuarios.
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario en la aplicación.
     * Construye un objeto {@link Patient} a partir de los datos de la solicitud de registro,
     * codifica la contraseña y la guarda en la base de datos. Luego, genera un token JWT
     * para el nuevo usuario.
     *
     * @param request El objeto {@link RegisterRequest} que contiene los detalles del nuevo usuario.
     * @return Un objeto {@link AuthResponse} que contiene el token JWT generado para el usuario registrado.
     */
    @Override // Anotación: Indica que este método sobrescribe un método de la interfaz AuthService.
    public AuthResponse register(RegisterRequest request) {
        // Construye un nuevo objeto User utilizando el patrón Builder.
        var user = Patient.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .email(request.getEmail())         // Establece el email (que servirá como nombre de usuario).
                .password(passwordEncoder.encode(request.getPassword())) // Codifica la contraseña antes de almacenarla.
                .role(Role.PATIENT) // Asigna el rol predeterminado de 'USER' al nuevo usuario.
                .build();   // Finaliza la construcción del objeto User.

        patientRepository.save(user);  // Guarda el nuevo objeto User en la base de datos.
        var jwtToken = jwtService.generateToken(user);  // Genera un token JWT para el usuario recién registrado.

        // Construye y retorna la respuesta de autenticación con el token JWT.
        return AuthResponse.builder().token(jwtToken).build();
    }

    /**
     * Autentica un usuario existente en la aplicación.
     * Intenta autenticar al usuario utilizando el email y la contraseña proporcionados.
     * Si la autenticación es exitosa, recupera los detalles del usuario y genera un token JWT.
     *
     * @param request El objeto {@link AuthenticationRequest} que contiene las credenciales del usuario (email y contraseña).
     * @return Un objeto {@link AuthResponse} que contiene el token JWT para la sesión autenticada.
     * @throws org.springframework.security.core.AuthenticationException Si las credenciales son inválidas.
     */
    @Override // Anotación: Indica que este método sobrescribe un método de la interfaz AuthService.
    public AuthResponse authenticate(AuthenticationRequest request) {

        try {
            // Intenta autenticar al usuario usando el AuthenticationManager.
            // Se crea un UsernamePasswordAuthenticationToken con el email y la contraseña.
            // Si las credenciales son incorrectas, el AuthenticationManager lanzará una excepción.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),  // Email del usuario como nombre de usuario.
                            request.getPassword() // Contraseña del usuario.
                    )
            );
        } catch (BadCredentialsException e) {
            // Aquí es donde el AuthenticationManager falla si las credenciales son incorrectas
            // No lances una RuntimeException aquí, déjala propagar o maneja con un ControllerAdvice.
            // Si no usas ControllerAdvice, podrías lanzar una excepción personalizada aquí
            // que luego tu controlador atrape para devolver un ResponseEntity adecuado.
            throw new BadCredentialsException("Credenciales de usuario o contraseña inválidas."); // Este mensaje se propagará.
        }

        // Si la autenticación fue exitosa (no se lanzó ninguna excepción),
        // busca el usuario en la base de datos usando el email.
        // .orElseThrow(): Lanza una excepción si el usuario no es encontrado (aunque a estas alturas,
        // ya debería existir si la autenticación fue exitosa).
        var user = patientRepository.findByEmail(request.getEmail()).
                orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado después de autenticación exitosa."
                )); // Esto no debería pasar si authenticationManager tuvo éxito

        var jwtToken = jwtService.generateToken(user); // Genera un token JWT para el usuario autenticado.

        // Construye y retorna la respuesta de autenticación con el token JWT.
        return AuthResponse
                .builder()
                .token(jwtToken)
                .patientId(user.getPatientId())
                .build();

    }

//    @Override
//    public void logout(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Authentication authentication
//    ) {
//        final String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return; // No hay token para invalidar
//        }
//        final String jwt = authHeader.substring(7);
//
//        // Buscar el token en tu base de datos o almacenamiento de tokens
//        // Marcarlo como "revocado" o "expirado" en una tabla de tokens
//        // O añadirlo a una lista negra si es stateless completamente
//        var storedToken = token.findToken(jwt); // Suponiendo que tienes un método así
//        if (storedToken != null) {
//            storedToken.setRevoked(true); // O similar
//            tokenRepository.save(storedToken);
//        }
//    }

}
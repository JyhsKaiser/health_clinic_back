package jyhs.health_clinic_back.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro personalizado de JWT que se ejecuta una vez por cada solicitud.
 * Se encarga de interceptar las peticiones HTTP y validar el token JWT
 * para establecer la autenticación del usuario en el contexto de seguridad.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // Servicio que carga los detalles del usuario (por ejemplo, desde la base de datos)
    private final @NonNull UserDetailsService userDetailsService;

    // Servicio encargado de generar, extraer y validar los tokens JWT
    private final @NonNull JwtService jwtService;

    /**
     * Método principal del filtro que intercepta cada solicitud y aplica la lógica de validación del JWT.
     *
     * @param request     la solicitud HTTP entrante
     * @param response    la respuesta HTTP saliente
     * @param filterChain la cadena de filtros a continuar
     * @throws ServletException en caso de error del servlet
     * @throws IOException      en caso de error de entrada/salida
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtener el valor del encabezado "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Verificar si hay un encabezado de autorización y si es un token Bearer
        // Si no hay token o no comienza con "Bearer", se continúa sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            // Si no hay token o no es un token Bearer, simplemente continúa la cadena de filtros.
            // Spring Security se encargará de las reglas de autorización (permitAll() vs authenticated()).
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token JWT
        // Extraer el token JWT removiendo el prefijo "Bearer "
        jwt = authHeader.substring(7);

        // 3. Extraer el email del usuario del token
        // Obtener el correo electrónico o nombre de usuario desde el token
        userEmail = jwtService.getUserName(jwt);

        // 4. Si el email existe y no hay una autenticación actual en el contexto de seguridad
        // Verificar que el usuario no esté ya autenticado en el contexto de seguridad
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5. Cargar los detalles del usuario
            // Cargar los detalles del usuario desde la base de datos o sistema
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validar el token y la validez del usuario
            // Validar el token JWT contra los datos del usuario
            if (jwtService.validateToken(jwt, userDetails)) {

                // 7. Crear un objeto de autenticación
                // Crear un objeto de autenticación con los datos del usuario
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // Contraseña es null para autenticación basada en token
                                userDetails.getAuthorities()
                        );


                // Asociar detalles adicionales del request a la autenticación (como IP, sesión, etc.)
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Establecer el objeto de autenticación en el SecurityContextHolder
                // Esto indica a Spring Security que el usuario está autenticado para esta solicitud.
                // Establecer la autenticación en el contexto de seguridad de Spring
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 9. Continuar con la cadena de filtros de Spring Security
        // ¡Esto es crucial para que las reglas de authorizeHttpRequests se apliquen!
        // Continuar con la ejecución de los siguientes filtros
        filterChain.doFilter(request, response);
    }

}

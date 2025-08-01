package jyhs.health_clinic_back.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull; // Importación para @NonNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException; // Importación para manejar token expirado
import io.jsonwebtoken.security.SignatureException; // Importación para manejar firma inválida

import java.io.IOException;
import java.util.Arrays; // Importación para Arrays.asList

/**
 * Filtro JWT personalizado que intercepta las peticiones HTTP para validar los tokens JWT.
 * Se encarga de extraer el token del encabezado, validarlo y configurar el contexto de seguridad de Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Spring Security UserDetailsService

    // Define los endpoints que son públicos y no requieren validación de JWT
    // ¡Estos deben coincidir con los de publicEndpoints() en SecurityConfig!
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/authenticate", // Endpoint de login
            "/api/v1/auth/register",     // Endpoint de registro
            // Agrega aquí cualquier otro endpoint que sea publico y no requiera JWT
            // Por ejemplo: "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password"
    };

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Verificar si la ruta es pública (no requiere JWT)
        // Si la ruta actual coincide con alguno de los endpoints públicos,
        // simplemente dejamos que la solicitud pase al siguiente filtro.
        // Spring Security se encargará de la autorización con permitAll().
        if (Arrays.stream(PUBLIC_ENDPOINTS).anyMatch(request.getRequestURI()::startsWith)) {
            filterChain.doFilter(request, response);
            return; // Detener el procesamiento del filtro JWT para rutas públicas
        }

        // 2. Extraer el token JWT del encabezado de autorización
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si no hay encabezado Authorization o no empieza con "Bearer ",
        // la solicitud no tiene un JWT válido para validar.
        // Se deja pasar al siguiente filtro, y Spring Security decidirá
        // si la ruta requiere autenticación y la denegará si es necesario.
        // NO se debe lanzar una excepción aquí, ya que podría ser una ruta protegida
        // sin token, y Spring Security debería manejar el 401/403.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token (ignorando "Bearer ")
        jwt = authHeader.substring(7);

        // 3. Validar el token y configurar el contexto de seguridad
        try {
            // Obtener el nombre de usuario (subject) del token
            userEmail = jwtService.getUserName(jwt);

            // Si el nombre de usuario existe y no hay una autenticación previa en el contexto
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cargar los detalles del usuario desde el UserDetailsService
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validar si el token es válido para el usuario (firma, expiración, etc.)
                // Asumo que jwtService.isTokenValid es el equivalente a tu jwtService.validateToken
                if (jwtService.validateToken(jwt, userDetails)) {
                    // Crear un objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credenciales son null para JWT ya que el token ya es la credencial
                            userDetails.getAuthorities() // Roles/Autoridades del usuario
                    );
                    // Establecer detalles de la solicitud (dirección IP, sesión, etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Guardar el objeto de autenticación en el SecurityContextHolder
                    // Esto indica a Spring Security que el usuario está autenticado para esta solicitud
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // Manejar token expirado: Limpiar contexto y enviar 401
            logger.warn("JWT Token expirado: {}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Token de autenticación caducado. Por favor, inicie sesión de nuevo.\"}");
            return; // Detener la cadena de filtros aquí
        } catch (SignatureException e) {
            // Manejar firma inválida: Limpiar contexto y enviar 403 (o 401)
            logger.warn("JWT Firma inválida: {}");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden (o 401)
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Token de autenticación inválido.\"}");
            return; // Detener la cadena de filtros aquí
        } catch (Exception e) {
            // Manejar otras excepciones durante la validación del token
            logger.error("Error al procesar JWT: {}");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Error interno del servidor al procesar el token.\"}");
            return; // Detener la cadena de filtros aquí
        }

        // Continuar con la cadena de filtros.
        // Si el usuario fue autenticado, los siguientes filtros y controladores lo verán como autenticado.
        filterChain.doFilter(request, response);
    }
}

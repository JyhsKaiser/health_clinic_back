package jyhs.health_clinic_back.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; // Necesario para decodificar la clave de base64
import io.jsonwebtoken.security.Keys; // Necesario para crear SecretKey
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey; // Importa SecretKey
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtService {

    // Es crucial que esta SECRET_KEY se maneje de forma segura.
    // Usar un string hardcodeado directamente en el código es un riesgo de seguridad.
    // Debería cargarse desde variables de entorno, un almacén de claves, o similar.
    private static final String SECRET_KEY = "c05778d47e8d2662b4920ac89867aede2813ea5034afefad04804411c0927f93";

    public String generateToken(UserDetails userDetails) {

        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Obtenemos la fecha de emisión (ahora)
        Date issuedAt = new Date(System.currentTimeMillis());
        // Calculamos la fecha de expiración (24 horas desde ahora)
        // Usamos TimeUnit para mayor claridad sobre la unidad de tiempo
        Date expiration = new Date(issuedAt.getTime() + TimeUnit.HOURS.toMillis(24));

        return Jwts.builder()
                // setClaims() ha sido reemplazado. Los claims se añaden directamente.
                .claims(extraClaims) // Añade tus claims adicionales
                .subject(userDetails.getUsername()) // Establece el 'subject'
                .issuedAt(issuedAt) // Establece la fecha de emisión 'iat'
                .expiration(expiration) // Establece la fecha de expiración 'exp'
                // signWith() ha sido reemplazado por un método que toma solo la SecretKey
                // El algoritmo se infiere del tipo de SecretKey (HMAC-SHA en este caso)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Usa signWith() con la SecretKey. El algoritmo se infiere.
                .compact();
    }

    /**
     * Extrae el nombre de usuario (subject) del token JWT.
     * @param token El token JWT a procesar.
     * @return El nombre de usuario.
     */
    public String getUserName(String token) {

        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token JWT.
     * @param token El token JWT a procesar.
     * @param claimsResolver Una función para resolver el claim deseado.
     * @return El valor del claim.
     * @param <T> El tipo del claim.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene la clave de firma/verificación.
     * Esta clave debería ser una SecretKey. La estás proporcionando como un String
     * y necesita ser convertida a un formato de clave binaria adecuado.
     * @return La SecretKey usada para firmar/verificar tokens.
     */
    private SecretKey getSignInKey() {
        // Decodifica la clave base64 y la convierte en una SecretKey.
        // Asegúrate de que tu SECRET_KEY sea lo suficientemente larga (mínimo 256 bits para HS256).
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Obtiene todos los claims del token JWT.
     * @param token El token JWT a procesar.
     * @return Los Claims contenidos en el token.
     */
    private Claims getAllClaims(String token) {
        return Jwts.parser() // ¡Directamente Jwts.parser()! Esto ya te devuelve un JwtParserBuilder
                .verifyWith(getSignInKey()) // Usa verifyWith() con la SecretKey obtenida
                .build()    //
                .parseSignedClaims(token) // Usa parseSignedClaims() para JWTs firmados
                .getPayload(); // Usa getPayload() para obtener el cuerpo (Claims)
    }


    /**
     * Valida si un token JWT es válido para un usuario específico.
     * Compara el nombre de usuario extraído del token con el nombre de usuario
     * de los detalles del usuario y verifica si el token no ha expirado.
     *
     * @param token El token JWT a validar.
     * @param userDetails Los detalles del usuario contra los que se valida el token.
     * @return {@code true} si el token es válido para el usuario y no ha expirado; {@code false} en caso contrario.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = getUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    /**
     * Verifica si un token JWT ha expirado.
     * Compara la fecha de expiración del token con la fecha y hora actuales.
     *
     * @param token El token JWT a verificar.
     * @return {@code true} si el token ha expirado; {@code false} en caso contrario.
     */
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }


    /**
     * Extrae la fecha de expiración del token JWT.
     * Utiliza el método {@code getClaim} para obtener el claim de expiración (exp).
     *
     * @param token El token JWT del que se extraerá la fecha de expiración.
     * @return La fecha de expiración del token.
     */
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }
}


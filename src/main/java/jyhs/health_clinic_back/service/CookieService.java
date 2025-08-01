package jyhs.health_clinic_back.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public void addHttpOnlyCookie(String name, String value, int maxAge, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // Hace la cookie inaccesible para JavaScript
        cookie.setSecure(true); // Solo se envía a través de HTTPS
        cookie.setPath("/"); // La cookie está disponible para toda la aplicación
        cookie.setMaxAge(maxAge); // Duración de la cookie
        response.addCookie(cookie); // Agrega la cookie a la respuesta HTTP

    }

    public void deleteCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

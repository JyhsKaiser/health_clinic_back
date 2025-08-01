package jyhs.health_clinic_back.service;
import jakarta.servlet.http.HttpServletResponse;
import jyhs.health_clinic_back.controller.models.AuthResponse;
import jyhs.health_clinic_back.controller.models.AuthenticationRequest;
import jyhs.health_clinic_back.controller.models.RegisterRequest;


public interface AuthService {
    AuthResponse register (RegisterRequest request);
    AuthResponse authenticate(AuthenticationRequest request);



//    AuthResponse authenticate(AuthenticationRequest request, HttpServletResponse response);

}

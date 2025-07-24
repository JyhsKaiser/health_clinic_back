package jyhs.health_clinic_back.service;
import jyhs.health_clinic_back.controller.models.AuthResponse;
import jyhs.health_clinic_back.controller.models.AuthenticationRequest;
import jyhs.health_clinic_back.controller.models.RegisterRequest;


public interface AuthService {
    AuthResponse register (RegisterRequest request);
    AuthResponse authenticate (AuthenticationRequest request);

}

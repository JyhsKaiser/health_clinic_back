package jyhs.health_clinic_back.controller;


import jakarta.servlet.http.HttpServletResponse;
import jyhs.health_clinic_back.entity.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jyhs.health_clinic_back.controller.models.AuthResponse;
import jyhs.health_clinic_back.controller.models.AuthenticationRequest;
import jyhs.health_clinic_back.controller.models.RegisterRequest;
import jyhs.health_clinic_back.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        try {
//            authService.register(request);
            return ResponseEntity.ok(authService.authenticate(request));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response){
        try {

            return ResponseEntity.ok(authService.authenticate(request, response));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

    }
//    @PostMapping("/authenticate")
//    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthenticationRequest request){
//        return ResponseEntity.ok(authService.authenticate(request));
//    }




    // --- Nuevo método de logout ---
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Authentication authentication // Spring Security inyecta el objeto Authentication
//    ) {
//        authService.logout(request, response, authentication);
//        return ResponseEntity.ok().build(); // O puedes enviar un mensaje de éxito
//    }

}

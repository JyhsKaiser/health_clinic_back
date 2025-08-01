package jyhs.health_clinic_back.entity;


import jyhs.health_clinic_back.controller.models.AuthResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ApiMessage {
    private String message;
}

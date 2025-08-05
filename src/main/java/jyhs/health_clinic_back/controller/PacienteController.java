package jyhs.health_clinic_back.controller;

import jyhs.health_clinic_back.entity.Patient;
import jyhs.health_clinic_back.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient")
//@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {
    @Autowired
    PatientService patientService;

    @GetMapping
    public ResponseEntity<Patient> getPatientById(@RequestParam Long id) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patient);
    }

    @PatchMapping
    public ResponseEntity<Patient> patchPatient(@RequestBody Patient patient) {
        Patient patientUpdate = patientService.patchPatientByBody(patient);
        if (patientUpdate == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patientUpdate);
    }

}

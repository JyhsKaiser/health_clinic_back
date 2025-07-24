package jyhs.health_clinic_back.service;

import jyhs.health_clinic_back.repository.PatientRepository;
import jyhs.health_clinic_back.entity.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }
}

package jyhs.health_clinic_back.services;

import jyhs.health_clinic_back.dao.PacienteRepository;
import jyhs.health_clinic_back.entity.Paciente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    public List<Paciente> findAll() {
        return pacienteRepository.findAll();
    }
}

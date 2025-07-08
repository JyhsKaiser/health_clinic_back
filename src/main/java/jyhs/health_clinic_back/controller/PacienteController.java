package jyhs.health_clinic_back.controller;

import jyhs.health_clinic_back.dao.PacienteRepository;
import jyhs.health_clinic_back.entity.Paciente;
import jyhs.health_clinic_back.services.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/paciente")
//@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {
    @Autowired
    PacienteService pacienteService;

    @GetMapping
    public List<Paciente> getAllPacientes() {
        System.out.println("hola");
        return pacienteService.findAll();
    }

}

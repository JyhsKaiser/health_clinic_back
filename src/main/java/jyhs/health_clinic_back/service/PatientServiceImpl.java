package jyhs.health_clinic_back.service;

import jyhs.health_clinic_back.entity.Patient;
import jyhs.health_clinic_back.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;



    @Override
    public Patient getPatientById(Long id) {
        Patient patient = patientRepository.getPatientByPatientId(id);
        if (patient != null) {
            return patient;
        }
        return null;
    }

    @Override
    public Patient patchPatientById(Patient patient)
    {
        Optional<Patient> existingPatient = patientRepository.findById(patient.getPatientId());
        if (existingPatient.isPresent() && existingPatient.get().getEnabled() == null)
        {
            Patient updatedPatient = existingPatient.get();

            updatedPatient.setPhone(patient.getPhone());
            updatedPatient.setAddress(patient.getAddress());
            updatedPatient.setWeight(patient.getWeight());
            updatedPatient.setHeight(patient.getHeight());
            updatedPatient.setAge(patient.getAge());
            updatedPatient.setGender(patient.getGender());
            updatedPatient.setBloodType(patient.getBloodType());
            updatedPatient.setEnabled(patient.getEnabled());

            patientRepository.save(updatedPatient);
            return updatedPatient;
        } else if (existingPatient.isPresent()) {
            Patient updatedPatient = existingPatient.get();

            updatedPatient.setPhone(patient.getPhone());
            updatedPatient.setAddress(patient.getAddress());
            updatedPatient.setWeight(patient.getWeight());
            updatedPatient.setHeight(patient.getHeight());
            updatedPatient.setAge(patient.getAge());
//            updatedPatient.setEnabled(patient.getEnabled());

            patientRepository.save(updatedPatient);
            return updatedPatient;
        }

        return null;

    }
}

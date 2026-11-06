package com.hospital.service;

import com.hospital.model.Patient;
import com.hospital.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired(required = false)
    private HospitalMessageProducer messageProducer;

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
    }

    public Patient save(Patient patient) {
        Patient saved = patientRepository.save(patient);
        if (messageProducer != null) {
            try {
                com.hospital.dto.MessagePayloadDTO msg = com.hospital.dto.MessagePayloadDTO.builder()
                    .content("Nuevo paciente registrado: " + saved.getFirstName() + " " + saved.getLastName() + " (RUT: " + saved.getRut() + ")")
                    .recipient(saved.getEmail())
                    .build();
                messageProducer.sendNotification(msg);
            } catch (Exception ex) {
                // Desacoplado: no bloquea la persistencia del paciente
            }
        }
        return saved;
    }

    public void deleteById(Long id) {
        patientRepository.deleteById(id);
    }

    public Patient findByEmail(String email) {
        return patientRepository.findPatientByEmail(email);
    }

}

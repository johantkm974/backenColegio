package com.example.demo.service.impl;

import com.example.demo.dto.AmbienteRequest;
import com.example.demo.dto.AmbienteResponse;
import com.example.demo.model.Ambiente;
import com.example.demo.model.Piso;
import com.example.demo.repository.AmbienteRepository;
import com.example.demo.repository.PisoRepository;
import com.example.demo.service.AmbienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AmbienteServiceImpl implements AmbienteService {

    private final AmbienteRepository ambienteRepository;
    private final PisoRepository pisoRepository;

    public AmbienteServiceImpl(AmbienteRepository ambienteRepository, PisoRepository pisoRepository) {
        this.ambienteRepository = ambienteRepository;
        this.pisoRepository = pisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteResponse> listarTodos() {
        return ambienteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AmbienteResponse> buscarPorId(Integer id) {
        return ambienteRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbienteResponse> listarPorPiso(Integer pisoId) {
        return ambienteRepository.findByPiso_Id(pisoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AmbienteResponse crear(AmbienteRequest request) {
        Piso piso = pisoRepository.findById(request.pisoId())
                .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado"));

        Ambiente ambiente = new Ambiente(
                piso,
                request.tipo(),
                request.especialidad(),
                request.codigo(),
                request.nombre()
        );
        return toResponse(ambienteRepository.save(ambiente));
    }

    @Override
    public Optional<AmbienteResponse> actualizar(Integer id, AmbienteRequest request) {
        return ambienteRepository.findById(id).map(ambiente -> {
            ambiente.setTipo(request.tipo());
            ambiente.setEspecialidad(request.especialidad());
            ambiente.setCodigo(request.codigo());
            ambiente.setNombre(request.nombre());
            if (request.pisoId() != null) {
                Piso piso = pisoRepository.findById(request.pisoId())
                        .orElseThrow(() -> new IllegalArgumentException("Piso no encontrado"));
                ambiente.setPiso(piso);
            }
            return toResponse(ambienteRepository.save(ambiente));
        });
    }

    @Override
    public Optional<AmbienteResponse> cambiarEstado(Integer id, Boolean activo) {
        return ambienteRepository.findById(id).map(ambiente -> {
            ambiente.setActivo(activo);
            return toResponse(ambienteRepository.save(ambiente));
        });
    }

    @Override
    public void eliminar(Integer id) {
        ambienteRepository.deleteById(id);
    }

    private AmbienteResponse toResponse(Ambiente a) {
        return new AmbienteResponse(
                a.getId(),
                a.getPiso().getId(),
                a.getTipo(),
                a.getEspecialidad(),
                a.getCodigo(),
                a.getNombre(),
                a.getActivo()
        );
    }
}

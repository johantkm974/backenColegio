package com.example.demo.service.impl;

import com.example.demo.dto.PisoRequest;
import com.example.demo.dto.PisoResponse;
import com.example.demo.model.Edificio;
import com.example.demo.model.Piso;
import com.example.demo.repository.EdificioRepository;
import com.example.demo.repository.PisoRepository;
import com.example.demo.service.PisoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PisoServiceImpl implements PisoService {

    private final PisoRepository pisoRepository;
    private final EdificioRepository edificioRepository;

    public PisoServiceImpl(PisoRepository pisoRepository, EdificioRepository edificioRepository) {
        this.pisoRepository = pisoRepository;
        this.edificioRepository = edificioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PisoResponse> listarTodos() {
        return pisoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PisoResponse> buscarPorId(Integer id) {
        return pisoRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PisoResponse> listarPorEdificio(Integer edificioId) {
        return pisoRepository.findByEdificio_Id(edificioId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PisoResponse crear(PisoRequest request) {
        Edificio edificio = edificioRepository.findById(request.edificioId())
                .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado"));

        if (pisoRepository.existsByEdificio_IdAndNumeroPiso(request.edificioId(), request.numeroPiso())) {
            throw new IllegalArgumentException("Ya existe un piso con ese número en el edificio");
        }

        Piso piso = new Piso(edificio, request.numeroPiso(), request.nombre());
        return toResponse(pisoRepository.save(piso));
    }

    @Override
    public Optional<PisoResponse> actualizar(Integer id, PisoRequest request) {
        return pisoRepository.findById(id).map(piso -> {
            if (request.edificioId() != null) {
                Edificio edificio = edificioRepository.findById(request.edificioId())
                        .orElseThrow(() -> new IllegalArgumentException("Edificio no encontrado"));
                piso.setEdificio(edificio);
            }
            piso.setNumeroPiso(request.numeroPiso());
            piso.setNombre(request.nombre());
            return toResponse(pisoRepository.save(piso));
        });
    }

    @Override
    public Optional<PisoResponse> cambiarEstado(Integer id, Boolean activo) {
        return pisoRepository.findById(id).map(piso -> {
            piso.setActivo(activo);
            return toResponse(pisoRepository.save(piso));
        });
    }

    @Override
    public void eliminar(Integer id) {
        pisoRepository.deleteById(id);
    }

    private PisoResponse toResponse(Piso p) {
        return new PisoResponse(
                p.getId(),
                p.getEdificio().getId(),
                p.getEdificio().getCodigo(),
                p.getNumeroPiso(),
                p.getNombre(),
                p.getActivo()
        );
    }
}

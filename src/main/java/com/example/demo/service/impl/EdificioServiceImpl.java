package com.example.demo.service.impl;

import com.example.demo.dto.EdificioRequest;
import com.example.demo.dto.EdificioResponse;
import com.example.demo.model.Edificio;
import com.example.demo.repository.EdificioRepository;
import com.example.demo.service.EdificioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EdificioServiceImpl implements EdificioService {

    private final EdificioRepository edificioRepository;

    public EdificioServiceImpl(EdificioRepository edificioRepository) {
        this.edificioRepository = edificioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EdificioResponse> listarTodos() {
        return edificioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EdificioResponse> buscarPorId(Integer id) {
        return edificioRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EdificioResponse> buscarPorCodigo(String codigo) {
        return edificioRepository.findByCodigo(codigo).map(this::toResponse);
    }

    @Override
    public EdificioResponse crear(EdificioRequest request) {
        if (edificioRepository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException("Ya existe un edificio con ese código");
        }
        Edificio edificio = new Edificio(request.codigo(), request.nombre(), request.descripcion());
        return toResponse(edificioRepository.save(edificio));
    }

    @Override
    public Optional<EdificioResponse> actualizar(Integer id, EdificioRequest request) {
        return edificioRepository.findById(id).map(edificio -> {
            edificio.setCodigo(request.codigo());
            edificio.setNombre(request.nombre());
            edificio.setDescripcion(request.descripcion());
            return toResponse(edificioRepository.save(edificio));
        });
    }

    @Override
    public Optional<EdificioResponse> cambiarEstado(Integer id, Boolean activo) {
        return edificioRepository.findById(id).map(edificio -> {
            edificio.setActivo(activo);
            return toResponse(edificioRepository.save(edificio));
        });
    }

    @Override
    public void eliminar(Integer id) {
        edificioRepository.deleteById(id);
    }

    private EdificioResponse toResponse(Edificio e) {
        return new EdificioResponse(e.getId(), e.getCodigo(), e.getNombre(), e.getDescripcion(), e.getActivo());
    }
}

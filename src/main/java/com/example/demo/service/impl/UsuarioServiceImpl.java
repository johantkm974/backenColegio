package com.example.demo.service.impl;

import com.example.demo.dto.UsuarioRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResponse> buscarPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResponse> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).map(this::toResponse);
    }

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese DNI");
        }
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }

        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        Usuario usuario = new Usuario(
                rol,
                request.nombres(),
                request.apellidos(),
                request.dni(),
                request.correo(),
                passwordEncoder.encode(request.password()),
                request.telefono()
        );
        usuario.setActivo(true);

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public Optional<UsuarioResponse> actualizar(Long id, UsuarioRequest request) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombres(request.nombres());
            usuario.setApellidos(request.apellidos());
            usuario.setDni(request.dni());
            usuario.setCorreo(request.correo());
            usuario.setTelefono(request.telefono());
            if (request.password() != null && !request.password().isBlank()) {
                usuario.setPasswordHash(passwordEncoder.encode(request.password()));
            }
            if (request.rolId() != null) {
                Rol rol = rolRepository.findById(request.rolId())
                        .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
                usuario.setRol(rol);
            }
            return toResponse(usuarioRepository.save(usuario));
        });
    }

    @Override
    public Optional<UsuarioResponse> cambiarEstado(Long id, Boolean activo) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(activo);
            return toResponse(usuarioRepository.save(usuario));
        });
    }

    @Override
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario cargarEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getNombreCompleto(),
                u.getDni(),
                u.getCorreo(),
                u.getTelefono(),
                u.getActivo(),
                u.getRol().getId(),
                u.getRol().getNombre()
        );
    }

    @Override
    public void cambiarContrasena(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }
}

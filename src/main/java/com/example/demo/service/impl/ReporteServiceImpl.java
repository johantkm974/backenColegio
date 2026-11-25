package com.example.demo.service.impl;

import com.example.demo.dto.ReporteRequest;
import com.example.demo.dto.ReporteResponse;
import com.example.demo.model.Ambiente;
import com.example.demo.model.HistorialReporte;
import com.example.demo.model.Reporte;
import com.example.demo.model.ReporteFoto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.AmbienteRepository;
import com.example.demo.repository.HistorialReporteRepository;
import com.example.demo.repository.ReporteFotoRepository;
import com.example.demo.repository.ReporteRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository reporteRepository;
    private final AmbienteRepository ambienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialReporteRepository historialReporteRepository;
    private final ReporteFotoRepository reporteFotoRepository;
    private final CloudinaryService cloudinaryService;

    private static final String PREFIJO_TICKET = "REP";
    private static final String CARPETA_CLOUDINARY = "reporta/incidencias";
    private static final String ESTADO_INICIAL = "POR_ATENDER";
    private static final Set<String> PRIORIDADES_VALIDAS = Set.of("BAJA", "MEDIA", "ALTA", "URGENTE");
    private static final Set<String> ESTADOS_VALIDOS = Set.of("POR_ATENDER", "EN_PROCESO", "ATENDIDO", "RECHAZADO");

    public ReporteServiceImpl(ReporteRepository reporteRepository,
                              AmbienteRepository ambienteRepository,
                              UsuarioRepository usuarioRepository,
                              HistorialReporteRepository historialReporteRepository,
                              ReporteFotoRepository reporteFotoRepository,
                              CloudinaryService cloudinaryService) {
        this.reporteRepository = reporteRepository;
        this.ambienteRepository = ambienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialReporteRepository = historialReporteRepository;
        this.reporteFotoRepository = reporteFotoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarTodos() {
        return reporteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteResponse> buscarPorId(Long id) {
        return reporteRepository.findById(id).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteResponse> buscarPorCodigoTicket(String codigoTicket) {
        return reporteRepository.findByCodigoTicket(codigoTicket).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPorEstado(String estado) {
        return reporteRepository.findByEstado(estado).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPorDocente(Long docenteId) {
        return reporteRepository.findByDocente_Id(docenteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPorEncargado(Long encargadoId) {
        return reporteRepository.findByEncargado_Id(encargadoId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReporteResponse crear(ReporteRequest request, Long docenteId) {
        Ambiente ambiente = ambienteRepository.findById(request.ambienteId())
                .orElseThrow(() -> new IllegalArgumentException("Ambiente no encontrado"));

        Usuario docente = usuarioRepository.findById(docenteId)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado"));

        String prioridad = normalizarPrioridad(request.prioridad());
        String codigoTicket = generarCodigoTicket();

        // Todo reporte nuevo nace siempre en POR_ATENDER
        Reporte reporte = new Reporte(
                codigoTicket,
                ambiente,
                docente,
                null,
                request.titulo().trim(),
                request.descripcion().trim(),
                request.fotoUrl(),
                prioridad,
                ESTADO_INICIAL
        );
        reporte.setFechaReporte(LocalDateTime.now());
        reporte.setFechaActualizacion(LocalDateTime.now());

        Reporte saved = reporteRepository.save(reporte);
        registrarHistorial(saved, docente, null, saved.getEstado(), "Reporte creado");

        return toResponse(saved);
    }

    @Override
    public ReporteResponse actualizar(Long id, ReporteRequest request, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reporte no encontrado con id: " + id));

        validarEdicionPermitida(reporte, usuarioId);

        Ambiente ambiente = ambienteRepository.findById(request.ambienteId())
                .orElseThrow(() -> new IllegalArgumentException("Ambiente no encontrado"));

        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        reporte.setAmbiente(ambiente);
        reporte.setTitulo(request.titulo().trim());
        reporte.setDescripcion(request.descripcion().trim());
        if (request.prioridad() != null && !request.prioridad().isBlank()) {
            reporte.setPrioridad(normalizarPrioridad(request.prioridad()));
        }
        reporte.setFechaActualizacion(LocalDateTime.now());

        Reporte saved = reporteRepository.save(reporte);
        registrarHistorial(saved, autor, saved.getEstado(), saved.getEstado(), "Reporte editado por el docente");

        return toResponse(saved);
    }

    @Override
    public Optional<ReporteResponse> actualizarEstado(Long id, String nuevoEstado, String observacion, Long usuarioId) {
        String estadoNormalizado = normalizarEstado(nuevoEstado);
        String obsLimpia = (observacion != null && !observacion.isBlank()) ? observacion.trim() : null;

        return reporteRepository.findById(id).map(reporte -> {
            String estadoAnterior = reporte.getEstado();
            boolean estaCerrado = "ATENDIDO".equalsIgnoreCase(estadoAnterior)
                    || "RECHAZADO".equalsIgnoreCase(estadoAnterior);
            boolean pasaACerrado = "ATENDIDO".equalsIgnoreCase(estadoNormalizado)
                    || "RECHAZADO".equalsIgnoreCase(estadoNormalizado);
            boolean esReapertura = estaCerrado && !pasaACerrado;

            if ((pasaACerrado || esReapertura) && obsLimpia == null) {
                throw new IllegalArgumentException(
                        esReapertura
                                ? "Debe indicar el motivo para reabrir un reporte cerrado"
                                : "Debe indicar una observación para cerrar el reporte"
                );
            }

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            reporte.setEstado(estadoNormalizado);
            reporte.setObservacionAtencion(obsLimpia);
            reporte.setFechaActualizacion(LocalDateTime.now());
            if (pasaACerrado) {
                reporte.setFechaAtencion(LocalDateTime.now());
            } else if (esReapertura) {
                reporte.setFechaAtencion(null);
            }

            Reporte saved = reporteRepository.save(reporte);
            registrarHistorial(saved, usuario, estadoAnterior, estadoNormalizado, obsLimpia);

            return toResponse(saved);
        });
    }

    @Override
    public Optional<ReporteResponse> asignarEncargado(Long id, Long encargadoId) {
        return reporteRepository.findById(id).map(reporte -> {
            Usuario encargado = usuarioRepository.findById(encargadoId)
                    .orElseThrow(() -> new IllegalArgumentException("Encargado no encontrado"));
            reporte.setEncargado(encargado);
            reporte.setFechaActualizacion(LocalDateTime.now());
            return toResponse(reporteRepository.save(reporte));
        });
    }

    @Override
    public void eliminar(Long id) {
        List<ReporteFoto> fotos = reporteFotoRepository.findByReporteIdOrderByCreadoEnAsc(id);
        for (ReporteFoto foto : fotos) {
            cloudinaryService.eliminarFoto(foto.getPublicId());
        }
        reporteRepository.deleteById(id);
    }

    @Override
    public ReporteResponse subirFoto(Long id, MultipartFile foto, Long usuarioId) {
        return subirFotos(id, List.of(foto), usuarioId);
    }

    @Override
    public ReporteResponse subirFotos(Long id, List<MultipartFile> fotos, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reporte no encontrado con id: " + id));

        validarEdicionPermitida(reporte, usuarioId);

        if (fotos == null || fotos.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos una foto");
        }

        long yaGuardadas = reporteFotoRepository.countByReporteId(id);
        if (yaGuardadas + fotos.size() > MAX_FOTOS) {
            throw new IllegalArgumentException(
                    "Máximo " + MAX_FOTOS + " fotos por reporte (ya tiene " + yaGuardadas + ")");
        }

        String primeraUrl = null;
        for (MultipartFile foto : fotos) {
            if (foto == null || foto.isEmpty()) {
                throw new IllegalArgumentException("Una de las fotos está vacía");
            }
            CloudinaryService.FotoSubida subida = cloudinaryService.subirFoto(foto, CARPETA_CLOUDINARY);
            reporteFotoRepository.save(new ReporteFoto(reporte, subida.url(), subida.publicId()));
            if (primeraUrl == null) {
                primeraUrl = subida.url();
            }
        }

        // Compatibilidad: conservar la primera foto existente si ya tenía una
        if ((reporte.getFotoUrl() == null || reporte.getFotoUrl().isBlank()) && primeraUrl != null) {
            reporte.setFotoUrl(primeraUrl);
        }
        reporte.setFechaActualizacion(LocalDateTime.now());

        Reporte saved = reporteRepository.save(reporte);
        return toResponse(saved);
    }

    @Override
    public ReporteResponse eliminarFotoPorUrl(Long id, String url, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reporte no encontrado con id: " + id));

        validarEdicionPermitida(reporte, usuarioId);

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Debe indicar la URL de la foto a eliminar");
        }

        List<ReporteFoto> fotos = reporteFotoRepository.findByReporteIdOrderByCreadoEnAsc(id);
        ReporteFoto objetivo = fotos.stream()
                .filter(f -> url.trim().equals(f.getUrl()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La foto indicada no pertenece a este reporte"));

        cloudinaryService.eliminarFoto(objetivo.getPublicId());
        reporteFotoRepository.delete(objetivo);

        String nuevaPrimeraUrl = fotos.stream()
                .filter(f -> !f.getId().equals(objetivo.getId()))
                .map(ReporteFoto::getUrl)
                .findFirst()
                .orElse(null);

        reporte.setFotoUrl(nuevaPrimeraUrl);
        reporte.setFechaActualizacion(LocalDateTime.now());
        Reporte saved = reporteRepository.save(reporte);

        return toResponse(saved);
    }

    // ==================== REGLAS Y HELPERS ====================

    private void validarEdicionPermitida(Reporte reporte, Long usuarioId) {
        if (reporte.getDocente() == null || !reporte.getDocente().getId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el autor del reporte puede editarlo o modificar sus fotos"
            );
        }
        if (!ESTADO_INICIAL.equalsIgnoreCase(reporte.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede editar el reporte porque ya está en estado "
                            + reporte.getEstado().replace("_", " ")
            );
        }
    }

    private String normalizarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            return "MEDIA";
        }
        String limpia = prioridad.trim().toUpperCase();
        if (!PRIORIDADES_VALIDAS.contains(limpia)) {
            throw new IllegalArgumentException("Prioridad no válida: " + prioridad);
        }
        return limpia;
    }

    private String generarCodigoTicket() {
        int anio = Year.now().getValue();
        int numero = (int) (reporteRepository.count() + 1);
        String codigo = String.format("%s-%d-%04d", PREFIJO_TICKET, anio, numero);
        while (reporteRepository.existsByCodigoTicket(codigo)) {
            numero++;
            codigo = String.format("%s-%d-%04d", PREFIJO_TICKET, anio, numero);
        }
        return codigo;
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("Debe indicar un estado válido");
        }
        String limpio = estado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(limpio)) {
            throw new IllegalArgumentException("Estado no válido: " + estado);
        }
        return limpio;
    }

    private void registrarHistorial(Reporte reporte, Usuario usuario, String estadoAnterior,
                                    String estadoNuevo, String comentario) {
        String comentarioSeguro = (comentario != null && comentario.length() > 255)
                ? comentario.substring(0, 255)
                : comentario;
        HistorialReporte historial = new HistorialReporte(
                reporte, usuario, estadoAnterior, estadoNuevo, comentarioSeguro
        );
        historialReporteRepository.save(historial);
    }

    private ReporteResponse toResponse(Reporte r) {
        List<String> fotosUrls = reporteFotoRepository
                .findByReporteIdOrderByCreadoEnAsc(r.getId()).stream()
                .map(ReporteFoto::getUrl)
                .collect(Collectors.toList());

        var ambiente = r.getAmbiente();
        var piso = ambiente != null ? ambiente.getPiso() : null;
        var edificio = piso != null ? piso.getEdificio() : null;

        return new ReporteResponse(
                r.getId(),
                r.getCodigoTicket(),
                ambiente != null ? ambiente.getId() : null,
                ambiente != null ? ambiente.getNombre() : null,
                piso != null ? piso.getNombre() : null,
                edificio != null ? edificio.getCodigo() + " · " + edificio.getNombre() : null,
                r.getDocente() != null ? r.getDocente().getId() : null,
                r.getDocente() != null ? r.getDocente().getNombreCompleto() : null,
                r.getEncargado() != null ? r.getEncargado().getId() : null,
                r.getEncargado() != null ? r.getEncargado().getNombreCompleto() : null,
                r.getTitulo(),
                r.getDescripcion(),
                r.getFotoUrl(),
                r.getPrioridad(),
                r.getEstado(),
                r.getObservacionAtencion(),
                r.getFechaReporte(),
                r.getFechaAtencion(),
                fotosUrls
        );
    }
}

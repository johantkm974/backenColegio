package com.example.demo.service.impl;

import com.example.demo.dto.ReporteRequest;
import com.example.demo.dto.ReporteResponse;
import com.example.demo.model.Ambiente;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

        String codigoTicket = generarCodigoTicket();

        Reporte reporte = new Reporte(
                codigoTicket,
                ambiente,
                docente,
                null, // encargado pendiente
                request.titulo(),
                request.descripcion(),
                request.fotoUrl(),
                request.prioridad() != null ? request.prioridad() : "MEDIA",
                request.estado() != null ? request.estado() : "POR_ATENDER"
        );
        reporte.setFechaReporte(LocalDateTime.now());
        reporte.setFechaActualizacion(LocalDateTime.now());

        Reporte saved = reporteRepository.save(reporte);

        // Registrar en historial
        registrarHistorial(saved, docente, null, saved.getEstado(), "Reporte creado");

        return toResponse(saved);
    }

    @Override
    public Optional<ReporteResponse> actualizarEstado(Long id, String nuevoEstado, String observacion, Long usuarioId) {
        return reporteRepository.findById(id).map(reporte -> {
            String estadoAnterior = reporte.getEstado();
            reporte.setEstado(nuevoEstado);
            reporte.setObservacionAtencion(observacion);
            reporte.setFechaActualizacion(LocalDateTime.now());
            if ("ATENDIDO".equalsIgnoreCase(nuevoEstado) || "RECHAZADO".equalsIgnoreCase(nuevoEstado)) {
                reporte.setFechaAtencion(LocalDateTime.now());
            }

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            Reporte saved = reporteRepository.save(reporte);
            registrarHistorial(saved, usuario, estadoAnterior, nuevoEstado, observacion);

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
        reporteRepository.deleteById(id);
    }

    private String generarCodigoTicket() {
        long count = reporteRepository.count();
        int numero = (int) (count + 1);
        return String.format("%s-%d-%04d", PREFIJO_TICKET, java.time.Year.now().getValue(), numero);
    }

    private void registrarHistorial(Reporte reporte, Usuario usuario, String estadoAnterior,
                                    String estadoNuevo, String comentario) {
        var historial = new com.example.demo.model.HistorialReporte(
                reporte, usuario, estadoAnterior, estadoNuevo, comentario
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

    @Override
    public ReporteResponse subirFoto(Long id, MultipartFile foto) {
        // Compatibilidad con el flujo de una sola foto: usa el mismo camino de múltiples fotos
        return subirFotos(id, List.of(foto));
    }

    @Override
    public ReporteResponse subirFotos(Long id, List<MultipartFile> fotos) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado con id: " + id));

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

        // Compatibilidad: la columna legacy foto_url queda con la primera foto
        if (primeraUrl != null) {
            reporte.setFotoUrl(primeraUrl);
        }
        reporte.setFechaActualizacion(LocalDateTime.now());

        Reporte saved = reporteRepository.save(reporte);
        return toResponse(saved);
    }
}

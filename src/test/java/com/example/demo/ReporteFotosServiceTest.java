package com.example.demo;

import com.example.demo.dto.ReporteResponse;
import com.example.demo.model.Ambiente;
import com.example.demo.model.Reporte;
import com.example.demo.model.Usuario;
import com.example.demo.repository.AmbienteRepository;
import com.example.demo.repository.HistorialReporteRepository;
import com.example.demo.repository.ReporteFotoRepository;
import com.example.demo.repository.ReporteRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ReporteService;
import com.example.demo.service.impl.ReporteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del flujo de múltiples fotos por reporte (tabla reporte_fotos + Cloudinary).
 *
 * <p>Unit tests puros con Mockito (sin contexto Spring): verifican el límite
 * de {@link ReporteService#MAX_FOTOS} fotos, que cada foto subida quede
 * persistida en reporte_fotos con su public_id, y el candado de edición
 * (solo el autor y solo mientras el reporte esté en POR_ATENDER).</p>
 */
@ExtendWith(MockitoExtension.class)
class ReporteFotosServiceTest {

    @Mock
    private ReporteRepository reporteRepository;
    @Mock
    private AmbienteRepository ambienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private HistorialReporteRepository historialReporteRepository;
    @Mock
    private ReporteFotoRepository reporteFotoRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    private ReporteService reporteService;
    private Reporte reporte;

    /** Simula la tabla reporte_fotos en memoria. */
    private final List<com.example.demo.model.ReporteFoto> tablaFotos = new ArrayList<>();

    @BeforeEach
    void setUp() {
        reporteService = new ReporteServiceImpl(
                reporteRepository,
                ambienteRepository,
                usuarioRepository,
                historialReporteRepository,
                reporteFotoRepository,
                cloudinaryService
        );

        // Docente dueño del reporte de prueba (id 10)
        Usuario docente = new Usuario();
        docente.setId(10L);

        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setTitulo("Luz quemada");
        reporte.setEstado("POR_ATENDER");
        reporte.setDocente(docente);
    }

    private MockMultipartFile archivo(String nombre) {
        return new MockMultipartFile("fotos", nombre, "image/jpeg", "contenido-imagen".getBytes());
    }

    @Test
    void subirFotosDebeSubirCadaFotoAYActualizarFotoUrlConLaPrimera() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteFotoRepository.countByReporteId(1L)).thenReturn(0L);

        // save() de reporte_fotos persiste en la "tabla" en memoria
        when(reporteFotoRepository.save(any(com.example.demo.model.ReporteFoto.class)))
                .thenAnswer(inv -> {
                    com.example.demo.model.ReporteFoto foto = inv.getArgument(0);
                    tablaFotos.add(foto);
                    return foto;
                });
        when(reporteFotoRepository.findByReporteIdOrderByCreadoEnAsc(1L)).thenAnswer(inv -> tablaFotos);
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));

        when(cloudinaryService.subirFoto(any(), anyString()))
                .thenAnswer(inv -> new CloudinaryService.FotoSubida(
                        "https://res.cloudinary.com/demo/image/upload/foto_" + inv.getArgument(0, MockMultipartFile.class).getOriginalFilename(),
                        "reporta/incidencias/" + inv.getArgument(0, MockMultipartFile.class).getOriginalFilename()));

        ReporteResponse respuesta = reporteService.subirFotos(1L, List.of(archivo("a.jpg"), archivo("b.jpg")), 10L);

        ArgumentCaptor<com.example.demo.model.ReporteFoto> captor =
                ArgumentCaptor.forClass(com.example.demo.model.ReporteFoto.class);
        verify(reporteFotoRepository, times(2)).save(captor.capture());
        assertEquals(2, tablaFotos.size(), "debe guardar 2 filas en reporte_fotos");

        // Compatibilidad: foto_url (columna legacy) toma la primera URL
        assertEquals("https://res.cloudinary.com/demo/image/upload/foto_a.jpg", reporte.getFotoUrl(),
                "foto_url debe quedar con la primera URL subida");

        // El DTO debe traer las URLs para que Android pinte las miniaturas
        assertEquals(2, respuesta.fotosUrls().size(), "fotosUrls debe tener 2 URLs");
        assertTrue(respuesta.fotosUrls().get(0).contains("res.cloudinary.com"));
    }

    @Test
    void subirFotosRechazaMasDeCincoFotosEnUnaSolicitud() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteFotoRepository.countByReporteId(1L)).thenReturn(0L);

        List<MultipartFile> seis = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            seis.add(archivo("f" + i + ".jpg"));
        }

        assertThrows(IllegalArgumentException.class, () -> reporteService.subirFotos(1L, seis, 10L),
                "6 fotos en una solicitud debe ser rechazado");
        verify(cloudinaryService, times(0)).subirFoto(any(), anyString());
    }

    @Test
    void subirFotosRechazaSiExcederiaElLimiteConLasYaGuardadas() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteFotoRepository.countByReporteId(1L)).thenReturn(4L);

        List<MultipartFile> dos = new ArrayList<>();
        dos.add(archivo("x1.jpg"));
        dos.add(archivo("x2.jpg"));

        assertThrows(IllegalArgumentException.class, () -> reporteService.subirFotos(1L, dos, 10L),
                "4 existentes + 2 nuevas = 6 debe ser rechazado");
        verify(cloudinaryService, times(0)).subirFoto(any(), anyString());
    }

    @Test
    void subirFotosRechazaListaVacia() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThrows(IllegalArgumentException.class, () -> reporteService.subirFotos(1L, List.of(), 10L),
                "lista vacía debe ser rechazada");
    }

    @Test
    void subirFotosRechazaCuandoElReporteYaEstaEnProceso() {
        reporte.setEstado("EN_PROCESO");
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> reporteService.subirFotos(1L, List.of(archivo("a.jpg")), 10L));
        verify(cloudinaryService, times(0)).subirFoto(any(), anyString());
    }

    @Test
    void subirFotosRechazaCuandoOtroUsuarioIntentaModificarElReporte() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> reporteService.subirFotos(1L, List.of(archivo("a.jpg")), 99L));
        verify(cloudinaryService, times(0)).subirFoto(any(), anyString());
    }
}

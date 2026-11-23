package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test de regresión: todas las entidades con columnas de fecha {@code NOT NULL}
 * deben inicializar sus timestamps antes del INSERT.
 *
 * <p>Columnas verificadas con {@code DESCRIBE} sobre {@code reporta_db}:<br>
 * - edificios.creado_en / actualizado_en → NOT NULL<br>
 * - ambientes.creado_en / actualizado_en → NOT NULL<br>
 * - usuarios.creado_en / actualizado_en → NOT NULL<br>
 * - historial_reportes.creado_en → NOT NULL<br>
 * - password_reset_tokens.creado_en → NOT NULL</p>
 *
 * <p>Nota: la tabla {@code pisos} NO tiene columnas de fecha, por eso
 * {@link Piso} no participa de este test.</p>
 */
@SpringBootTest
@Transactional
class FechasEntidadesTest {

    @Autowired private EdificioRepository edificioRepository;
    @Autowired private PisoRepository pisoRepository;
    @Autowired private AmbienteRepository ambienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private HistorialReporteRepository historialReporteRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private ReporteRepository reporteRepository;

    @Test
    void edificioDebeGuardarConFechas() {
        Edificio ed = new Edificio("ED-97", "Edificio Fechas", "test");
        Edificio guardado = edificioRepository.save(ed);
        edificioRepository.flush();

        assertNotNull(guardado.getCreadoEn(), "edificios.creado_en no debe ser NULL");
        assertNotNull(guardado.getActualizadoEn(), "edificios.actualizado_en no debe ser NULL");
    }

    @Test
    void ambienteDebeGuardarConFechas() {
        Edificio ed = new Edificio("ED-96", "Edificio Para Ambiente", "test");
        edificioRepository.save(ed);
        Piso piso = pisoRepository.save(new Piso(ed, 1, "Piso Test Fechas"));

        Ambiente amb = new Ambiente(piso, "AULA", null, null, "Aula Fechas Test");
        Ambiente guardado = ambienteRepository.save(amb);
        ambienteRepository.flush();

        assertNotNull(guardado.getCreadoEn(), "ambientes.creado_en no debe ser NULL");
        assertNotNull(guardado.getActualizadoEn(), "ambientes.actualizado_en no debe ser NULL");
    }

    @Test
    void usuarioDebeGuardarConFechas() {
        Rol rol = rolRepository.findById(3).orElseThrow();
        Usuario usuario = new Usuario(rol, "Test", "Fechas", "99887766",
                "test.fechas@institucion.edu.pe", "$2a$10$abcdefghijklmnopqrstuv", null);
        Usuario guardado = usuarioRepository.save(usuario);
        usuarioRepository.flush();

        assertNotNull(guardado.getCreadoEn(), "usuarios.creado_en no debe ser NULL");
        assertNotNull(guardado.getActualizadoEn(), "usuarios.actualizado_en no debe ser NULL");
    }

    @Test
    void historialReporteDebeGuardarConFecha() {
        // Cadena completa: rol → usuario → edificio → piso → ambiente → reporte → historial
        Rol rol = rolRepository.findById(3).orElseThrow();
        Usuario usuario = new Usuario(rol, "Hist", "Fechas", "99887764",
                "test.historial@institucion.edu.pe", "$2a$10$abcdefghijklmnopqrstuv", null);
        usuarioRepository.save(usuario);

        Edificio ed = new Edificio("ED-95", "Edificio Historial", "test");
        edificioRepository.save(ed);
        Piso piso = pisoRepository.save(new Piso(ed, 1, "Piso Historial"));
        Ambiente amb = ambienteRepository.save(new Ambiente(piso, "AULA", null, null, "Aula Historial"));

        Reporte reporte = new Reporte("REP-T-" + System.nanoTime(), amb, usuario, null,
                "Titulo historial", "Descripcion historial", null, "MEDIA", "POR_ATENDER");
        reporte.setFechaReporte(LocalDateTime.now());
        reporte.setFechaActualizacion(LocalDateTime.now());
        reporteRepository.save(reporte);

        HistorialReporte historial = new HistorialReporte(
                reporte, usuario, null, "POR_ATENDER", "Reporte creado");
        HistorialReporte guardado = historialReporteRepository.save(historial);
        historialReporteRepository.flush();

        assertNotNull(guardado.getCreadoEn(), "historial_reportes.creado_en no debe ser NULL");
    }

    @Test
    void passwordResetTokenDebeGuardarConFecha() {
        Rol rol = rolRepository.findById(3).orElseThrow();
        Usuario usuario = new Usuario(rol, "Reset", "Fechas", "99887765",
                "test.reset@institucion.edu.pe", "$2a$10$abcdefghijklmnopqrstuv", null);
        usuarioRepository.save(usuario);

        PasswordResetToken token = new PasswordResetToken(
                usuario, "token-test-" + System.nanoTime(), LocalDateTime.now().plusHours(1));
        PasswordResetToken guardado = passwordResetTokenRepository.save(token);
        passwordResetTokenRepository.flush();

        assertNotNull(guardado.getCreadoEn(), "password_reset_tokens.creado_en no debe ser NULL");
    }
}

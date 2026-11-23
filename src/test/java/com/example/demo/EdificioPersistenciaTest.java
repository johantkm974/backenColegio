package com.example.demo;

import com.example.demo.model.Edificio;
import com.example.demo.repository.EdificioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test de regresión: reproduce el error 500 al crear un edificio.
 *
 * <p>La columna {@code edificios.creado_en} es {@code DATETIME NOT NULL} en
 * MySQL (ver {@code Reporta_mysql.sql}), pero {@link Edificio} no inicializa
 * {@code creadoEn}/{@code actualizadoEn}. Hibernate envía {@code NULL} en el
 * INSERT y MySQL rechaza la fila con error 1048.</p>
 */
@SpringBootTest
@Transactional
class EdificioPersistenciaTest {

    @Autowired
    private EdificioRepository edificioRepository;

    @Test
    void crearEdificioDebeGuardarseConFechasNoNulas() {
        Edificio edificio = new Edificio("ED-99", "Edificio Test Regresion", "Solo para test");

        Edificio guardado = edificioRepository.save(edificio);
        edificioRepository.flush();

        assertNotNull(guardado.getCreadoEn(), "creadoEn no debe ser NULL después de guardar");
        assertNotNull(guardado.getActualizadoEn(), "actualizadoEn no debe ser NULL después de guardar");
    }
}

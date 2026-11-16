package com.casaaurora.esquema;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.persistence.EntityManagerFactory;

/** Verifica que el exportador lea el esquema real y los datos cargados al arrancar. */
@SpringBootTest
class ExportadorEsquemaTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private ConfigurableApplicationContext contexto;

    @Test
    void exportaLasCincoTablasConSusLlavesIndicesYPoliticas() throws Exception {
        String json = new ExportadorEsquema(dataSource, emf, contexto).exportar();

        assertTrue(json.contains("\"motor\": \"h2\""));
        for (String tabla : new String[] {"cliente", "insumo", "lote", "lote_insumo", "pedido"}) {
            assertTrue(json.contains("\"nombre\": \"" + tabla + "\""), "Falta la tabla " + tabla);
        }
        assertTrue(json.contains("\"pk\":true"));
        assertTrue(json.contains("idx_cliente_nombre"));
        assertTrue(json.contains("\"referencia\":\"cliente.id\""));
        assertTrue(json.contains("\"referencia\":\"lote.id\""));
        assertTrue(json.contains("pedido_lectura_solo_admin"));
        assertTrue(json.contains("\"filas\": 24"), "lote_insumo debería tener 24 filas");
    }
}
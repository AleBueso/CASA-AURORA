package com.casaaurora.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Pruebas unitarias de las reglas del modelo, sin base de datos. */
class ModeloTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);

    private Lote loteNuevo() {
        return new Lote(INICIO, new BigDecimal("19"), 8, 3, 24, "Prueba");
    }

    @Test
    void loteQuedaListoALos11Dias() {
        Lote lote = loteNuevo();
        assertEquals(LocalDate.of(2026, 11, 12), lote.fechaListaEstimada());
        assertEquals(24, lote.getBotellasEstimadas());
        assertNull(lote.getBotellasObtenidas());
    }

    @Test
    void alargarLaFermentacionCorreLaFechaLista() {
        Lote lote = loteNuevo();
        lote.alargarFermentacion(2);
        assertEquals(2, lote.getDiasExtra());
        assertEquals(LocalDate.of(2026, 11, 14), lote.fechaListaEstimada());
    }

    @Test
    void embotellarRegistraFechaYBotellas() {
        Lote lote = loteNuevo();
        lote.embotellar(LocalDate.of(2026, 11, 12), 23);
        assertEquals(Integer.valueOf(23), lote.getBotellasObtenidas());
        assertEquals(LocalDate.of(2026, 11, 12), lote.getFechaEmbotellado());
        assertEquals(INICIO, lote.getFechaInicio());
        assertEquals(new BigDecimal("19"), lote.getLitros());
        assertEquals(8, lote.getDiasFermentacion());
        assertEquals(3, lote.getDiasReposo());
        assertEquals("Prueba", lote.getObservaciones());
    }

    @Test
    void precioDeMayoreoDesde7Botellas() {
        assertEquals(new BigDecimal("300.00"), Pedido.precioPara(6));
        assertEquals(new BigDecimal("280.00"), Pedido.precioPara(7));
    }

    @Test
    void pedidoCalculaSuPrecioYSeMarcaEntregado() {
        Cliente cliente = new Cliente("María López", "9876-5432", "WhatsApp");
        Pedido pedido = new Pedido(cliente, 7, "WhatsApp", INICIO, INICIO.plusDays(3));
        assertEquals(new BigDecimal("280.00"), pedido.getPrecioUnitario());
        assertFalse(pedido.isEntregado());
        pedido.marcarEntregado();
        assertTrue(pedido.isEntregado());
        assertEquals(cliente, pedido.getCliente());
        assertEquals(7, pedido.getCantidad());
        assertEquals("WhatsApp", pedido.getCanal());
        assertEquals(INICIO, pedido.getFechaPedido());
        assertEquals(INICIO.plusDays(3), pedido.getFechaEntrega());
    }

    @Test
    void clienteInsumoYRecetaGuardanSusDatos() {
        Cliente cliente = new Cliente("Ana Zelaya", "8890-1123", "Facebook");
        assertEquals("Ana Zelaya", cliente.getNombre());
        assertEquals("8890-1123", cliente.getTelefono());
        assertEquals("Facebook", cliente.getCanalPreferido());

        Insumo flor = new Insumo("Flor de Jamaica", "lb");
        assertEquals("Flor de Jamaica", flor.getNombre());
        assertEquals("lb", flor.getUnidad());

        Lote lote = loteNuevo();
        LoteInsumo receta = new LoteInsumo(lote, flor, new BigDecimal("2.00"));
        assertEquals(lote, receta.getLote());
        assertEquals(flor, receta.getInsumo());
        assertEquals(new BigDecimal("2.00"), receta.getCantidad());
    }
}
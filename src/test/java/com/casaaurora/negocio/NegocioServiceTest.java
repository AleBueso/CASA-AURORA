package com.casaaurora.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.casaaurora.modelo.Cliente;
import com.casaaurora.modelo.Pedido;
import com.casaaurora.negocio.Vistas.EstadoPedido;
import com.casaaurora.negocio.Vistas.PedidoVista;

/** Pruebas de la función extraída en el refactor: clasificarPedido. */
class NegocioServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 19);

    private Pedido pedidoParaEl(LocalDate entrega) {
        Cliente cliente = new Cliente("Cliente de prueba", "9999-9999", "WhatsApp");
        return new Pedido(cliente, 12, "WhatsApp", HOY, entrega);
    }

    @Test
    void pedidoSinBotellasSuficientesQuedaSinProduccionYEnRiesgo() {
        PedidoVista v = NegocioService.clasificarPedido(pedidoParaEl(HOY.plusDays(5)), 4, 8,
                List.of("8 de bodega"), HOY);
        assertEquals(EstadoPedido.SIN_PRODUCCION, v.estado());
        assertNull(v.fechaDisponible());
        assertTrue(v.enRiesgo());
    }

    @Test
    void pedidoCubiertoSoloConBodegaQuedaCubierto() {
        PedidoVista v = NegocioService.clasificarPedido(pedidoParaEl(HOY.plusDays(5)), 0, 12,
                List.of("12 de bodega"), HOY);
        assertEquals(EstadoPedido.CUBIERTO, v.estado());
        assertEquals("12 botellas apartadas de bodega", v.detalle());
        assertFalse(v.enRiesgo());
    }

    @Test
    void pedidoQueDependeDeUnLoteTardioQuedaReservadoYEnRiesgo() {
        LocalDate disponible = HOY.plusDays(10);
        PedidoVista v = NegocioService.clasificarPedido(pedidoParaEl(HOY.plusDays(5)), 0, 0,
                List.of("12 del lote #6"), disponible);
        assertEquals(EstadoPedido.RESERVADO, v.estado());
        assertEquals("12 del lote #6 · disponible 29/11/2026", v.detalle());
        assertTrue(v.enRiesgo());
    }
}
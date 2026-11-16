package com.casaaurora.negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.casaaurora.lotes.EstadoLote;

/** Datos que la API entrega al panel. */
public final class Vistas {

    private Vistas() {
    }

    /** Estado de un pedido según la asignación por orden de llegada. */
    public enum EstadoPedido {
        CUBIERTO, RESERVADO, SIN_PRODUCCION, ENTREGADO
    }

    public record Resumen(
            int botellasDisponibles,
            int botellasComprometidas,
            int botellasEnBodega,
            int botellasProducidas,
            int botellasEnProduccion,
            int lotesEnProceso,
            int pedidosPendientes,
            List<String> alertas) {
    }

    public record LoteVista(
            Long id,
            LocalDate fechaInicio,
            BigDecimal litros,
            EstadoLote estado,
            long diasTranscurridos,
            LocalDate fechaListaEstimada,
            int diasExtra,
            int botellasEstimadas,
            Integer botellasObtenidas,
            LocalDate fechaEmbotellado,
            String observaciones) {
    }

    public record PedidoVista(
            Long id,
            String cliente,
            String telefono,
            String canal,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal total,
            LocalDate fechaPedido,
            LocalDate fechaEntrega,
            EstadoPedido estado,
            String detalle,
            LocalDate fechaDisponible,
            boolean enRiesgo) {
    }
}
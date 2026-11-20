package com.casaaurora.negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casaaurora.lotes.EstadoLote;
import com.casaaurora.modelo.Lote;
import com.casaaurora.modelo.Pedido;
import com.casaaurora.negocio.Vistas.EstadoPedido;
import com.casaaurora.negocio.Vistas.LoteVista;
import com.casaaurora.negocio.Vistas.PedidoVista;
import com.casaaurora.negocio.Vistas.Resumen;

/**
 * Reglas de negocio de Casa Aurora: estado de los lotes, inventario de botellas
 * y asignación de pedidos por orden de llegada.
 */
@Service
@Transactional(readOnly = true)
public class NegocioService {

    private static final ZoneId HONDURAS = ZoneId.of("America/Tegucigalpa");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LoteRepository lotes;
    private final PedidoRepository pedidos;

    public NegocioService(LoteRepository lotes, PedidoRepository pedidos) {
        this.lotes = lotes;
        this.pedidos = pedidos;
    }

    public LocalDate hoy() {
        return LocalDate.now(HONDURAS);
    }

    // ---------- Lotes ----------

    /** Estado del lote calculado a partir de la fecha de hoy. */
    public static EstadoLote estadoDe(Lote lote, LocalDate hoy) {
        if (lote.getFechaEmbotellado() != null) {
            return EstadoLote.EMBOTELLADO;
        }
        LocalDate finFermentacion = lote.getFechaInicio().plusDays((long) lote.getDiasFermentacion() + lote.getDiasExtra());
        if (hoy.isBefore(finFermentacion)) {
            return EstadoLote.FERMENTANDO;
        }
        if (hoy.isBefore(lote.fechaListaEstimada())) {
            return EstadoLote.REPOSANDO;
        }
        return EstadoLote.LISTO;
    }

    public List<LoteVista> lotes() {
        LocalDate hoy = hoy();
        return lotes.findAllByOrderByFechaInicioDescIdDesc().stream().map(l -> vista(l, hoy)).toList();
    }

    static LoteVista vista(Lote l, LocalDate hoy) {
        return new LoteVista(l.getId(), l.getFechaInicio(), l.getLitros(), estadoDe(l, hoy),
                Math.max(0, ChronoUnit.DAYS.between(l.getFechaInicio(), hoy)), l.fechaListaEstimada(),
                l.getDiasExtra(), l.getBotellasEstimadas(), l.getBotellasObtenidas(), l.getFechaEmbotellado(),
                l.getObservaciones());
    }

    // ---------- Pedidos e inventario ----------

    /** Resultado de asignar los pedidos pendientes a la bodega y a los lotes en proceso. */
    record Asignacion(Map<Long, PedidoVista> porPedido, int enBodega, int comprometidas) {
    }

    /** Botellas físicas en bodega = embotelladas − entregadas. */
    static int botellasEnBodega(List<Lote> todosLosLotes, List<Pedido> todosLosPedidos) {
        int embotelladas = botellasEmbotelladas(todosLosLotes);
        int entregadas = todosLosPedidos.stream().filter(Pedido::isEntregado).mapToInt(Pedido::getCantidad).sum();
        return embotelladas - entregadas;
    }

    /** Total de botellas que salieron de los lotes ya embotellados. */
    static int botellasEmbotelladas(List<Lote> todosLosLotes) {
        return todosLosLotes.stream()
                .mapToInt(l -> l.getBotellasObtenidas() == null ? 0 : l.getBotellasObtenidas()).sum();
    }

    /**
     * Asignación FIFO: los pedidos pendientes se atienden por orden de llegada (id),
     * primero con las botellas en bodega y luego con los lotes en proceso, por fecha de listo.
     * La fecha de entrega no cambia el orden: así nadie que pidió antes se queda sin vino.
     */
    static Asignacion asignar(List<Lote> todosLosLotes, List<Pedido> todosLosPedidos, LocalDate hoy) {
        int enBodega = botellasEnBodega(todosLosLotes, todosLosPedidos);
        int libresEnBodega = Math.max(0, enBodega);
        int comprometidas = 0;

        List<Lote> enProceso = new ArrayList<>(todosLosLotes.stream().filter(l -> l.getFechaEmbotellado() == null).toList());
        enProceso.sort(Comparator.comparing(Lote::fechaListaEstimada).thenComparing(Lote::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        Map<Lote, Integer> libresPorLote = new HashMap<>();
        enProceso.forEach(l -> libresPorLote.put(l, l.getBotellasEstimadas()));

        Map<Long, PedidoVista> resultado = new HashMap<>();
        List<Pedido> ordenados = new ArrayList<>(todosLosPedidos);
        ordenados.sort(Comparator.comparing(Pedido::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        for (Pedido p : ordenados) {
            if (p.isEntregado()) {
                resultado.put(p.getId(), vista(p, EstadoPedido.ENTREGADO, "Entregado", null, false));
                continue;
            }
            int falta = p.getCantidad();
            int deBodega = Math.min(falta, libresEnBodega);
            libresEnBodega -= deBodega;
            comprometidas += deBodega;
            falta -= deBodega;

            List<String> partes = new ArrayList<>();
            if (deBodega > 0) {
                partes.add(deBodega + " de bodega");
            }
            LocalDate disponible = deBodega > 0 ? hoy : null;
            for (Lote lote : enProceso) {
                if (falta == 0) {
                    break;
                }
                int toma = Math.min(falta, libresPorLote.get(lote));
                if (toma > 0) {
                    libresPorLote.put(lote, libresPorLote.get(lote) - toma);
                    falta -= toma;
                    partes.add(toma + " del lote #" + lote.getId());
                    LocalDate lista = lote.fechaListaEstimada().isBefore(hoy) ? hoy : lote.fechaListaEstimada();
                    disponible = disponible == null || lista.isAfter(disponible) ? lista : disponible;
                }
            }

            resultado.put(p.getId(), clasificarPedido(p, falta, deBodega, partes, disponible));
        }
        return new Asignacion(resultado, enBodega, comprometidas);
    }

    /**
     * Decide el estado de un pedido pendiente una vez repartidas las botellas:
     * SIN_PRODUCCION si aún faltan, CUBIERTO si todo salió de bodega y RESERVADO
     * si depende de lotes en proceso. También marca si llega tarde a la entrega.
     */
    static PedidoVista clasificarPedido(Pedido p, int falta, int deBodega, List<String> partes, LocalDate disponible) {
        if (falta > 0) {
            String detalle = "Faltan " + falta + " botellas: no hay lote que las cubra";
            return vista(p, EstadoPedido.SIN_PRODUCCION, detalle, null, true);
        }
        boolean enRiesgo = disponible.isAfter(p.getFechaEntrega());
        if (partes.size() == 1 && deBodega > 0) {
            return vista(p, EstadoPedido.CUBIERTO, deBodega + " botellas apartadas de bodega", disponible, enRiesgo);
        }
        String detalle = String.join(" + ", partes) + " · disponible " + disponible.format(FECHA);
        return vista(p, EstadoPedido.RESERVADO, detalle, disponible, enRiesgo);
    }

    private static PedidoVista vista(Pedido p, EstadoPedido estado, String detalle, LocalDate disponible, boolean enRiesgo) {
        BigDecimal total = p.getPrecioUnitario().multiply(BigDecimal.valueOf(p.getCantidad()));
        return new PedidoVista(p.getId(), p.getCliente().getNombre(), p.getCliente().getTelefono(), p.getCanal(),
                p.getCantidad(), p.getPrecioUnitario(), total, p.getFechaPedido(), p.getFechaEntrega(),
                estado, detalle, disponible, enRiesgo);
    }

    /** Pedidos por orden de llegada: primero los pendientes, después los entregados. */
    public List<PedidoVista> pedidos() {
        Asignacion a = asignar(lotes.findAll(), pedidos.findAll(), hoy());
        List<PedidoVista> lista = new ArrayList<>(a.porPedido().values());
        lista.sort(Comparator.comparing((PedidoVista v) -> v.estado() == EstadoPedido.ENTREGADO)
                .thenComparing(PedidoVista::id));
        return lista;
    }

    // ---------- Resumen del panel ----------

    public Resumen resumen() {
        LocalDate hoy = hoy();
        List<Lote> todosLosLotes = lotes.findAll();
        List<Pedido> todosLosPedidos = pedidos.findAll();
        Asignacion a = asignar(todosLosLotes, todosLosPedidos, hoy);

        int producidas = botellasEmbotelladas(todosLosLotes);
        List<Lote> enProceso = todosLosLotes.stream().filter(l -> l.getFechaEmbotellado() == null).toList();
        int enProduccion = enProceso.stream().mapToInt(Lote::getBotellasEstimadas).sum();
        int pendientes = (int) todosLosPedidos.stream().filter(p -> !p.isEntregado()).count();

        List<String> alertas = new ArrayList<>();
        enProceso.stream()
                .filter(l -> estadoDe(l, hoy) == EstadoLote.LISTO)
                .forEach(l -> alertas.add("El lote #" + l.getId() + " está listo para embotellar (~" + l.getBotellasEstimadas() + " botellas)."));
        a.porPedido().values().stream()
                .filter(v -> v.estado() != EstadoPedido.ENTREGADO)
                .sorted(Comparator.comparing(PedidoVista::id))
                .forEach(v -> {
                    if (v.estado() == EstadoPedido.SIN_PRODUCCION) {
                        alertas.add("Pedido #" + v.id() + " de " + v.cliente() + ": " + v.detalle() + ". Iniciá un lote nuevo.");
                    } else if (v.enRiesgo()) {
                        alertas.add("Pedido #" + v.id() + " de " + v.cliente() + " en riesgo: entrega "
                                + v.fechaEntrega().format(FECHA) + ", vino disponible " + v.fechaDisponible().format(FECHA) + ".");
                    }
                });

        return new Resumen(a.enBodega() - a.comprometidas(), a.comprometidas(), a.enBodega(), producidas,
                enProduccion, enProceso.size(), pendientes, alertas);
    }
}
package com.casaaurora.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Carga los datos al arrancar (ver ADR-001): la receta real de Casa Aurora,
 * los lotes del cuestionario de la clienta y pedidos de ejemplo con fechas relativas a hoy.
 */
@Component
@Order(1)
public class DatosIniciales implements CommandLineRunner {

    private static final int DIAS_FERMENTACION = 8;
    private static final int DIAS_REPOSO = 3;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        Long lotes = em.createQuery("select count(l) from Lote l", Long.class).getSingleResult();
        if (lotes > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Tegucigalpa"));

        // Insumos y receta real por micro lote de 19 L (5 galones)
        Insumo agua = guardar(new Insumo("Agua", "L"));
        Insumo flor = guardar(new Insumo("Flor de Jamaica", "lb"));
        Insumo azucar = guardar(new Insumo("Azúcar", "lb"));
        Insumo levadura = guardar(new Insumo("Levadura", "g"));

        // Lotes: tres ya embotellados (del cuestionario) y tres en proceso
        crearLote(LocalDate.of(2026, 9, 27), "38", 0, 48, "Dos micro lotes de 5 galones", agua, flor, azucar, levadura);
        crearLote(LocalDate.of(2026, 10, 4), "38", 2, 48, "Fermentación alargada por la temperatura", agua, flor, azucar, levadura);
        crearLote(LocalDate.of(2026, 10, 24), "113", 0, 144, "Producción grande para noviembre", agua, flor, azucar, levadura);
        crearLote(hoy.minusDays(9), "19", 0, null, null, agua, flor, azucar, levadura);
        crearLote(hoy.minusDays(6), "19", 3, null, "Alargado 3 días por la temperatura", agua, flor, azucar, levadura);
        crearLote(hoy.minusDays(2), "38", 0, null, null, agua, flor, azucar, levadura);

        // Clientes de ejemplo
        Cliente maria = guardar(new Cliente("María López", "9876-5432", "WhatsApp"));
        Cliente fogon = guardar(new Cliente("Restaurante El Fogón", "3312-4455", "En persona"));
        Cliente carlos = guardar(new Cliente("Carlos Mejía", "9541-2200", "Instagram"));
        Cliente tienda = guardar(new Cliente("Tienda La Esquina", "2763-1188", "En persona"));
        Cliente ana = guardar(new Cliente("Ana Zelaya", "8890-1123", "Facebook"));
        Cliente feria = guardar(new Cliente("Feria de Emprendedores Danlí", "9900-7766", "Feria"));

        // Pedidos en orden de llegada
        List<Pedido> pedidos = List.of(
                new Pedido(maria, 6, "WhatsApp", hoy.minusDays(22), hoy.minusDays(18)),
                new Pedido(fogon, 24, "En persona", hoy.minusDays(15), hoy.minusDays(10)),
                new Pedido(carlos, 12, "Instagram", hoy.minusDays(8), hoy.minusDays(3)),
                new Pedido(tienda, 18, "En persona", hoy.minusDays(6), hoy.plusDays(1)),
                new Pedido(ana, 8, "Facebook", hoy.minusDays(5), hoy.plusDays(2)),
                new Pedido(feria, 36, "Feria", hoy.minusDays(4), hoy.plusDays(4)),
                new Pedido(maria, 6, "WhatsApp", hoy.minusDays(2), hoy.plusDays(5)),
                new Pedido(carlos, 30, "Instagram", hoy.minusDays(1), hoy.plusDays(7)));
        for (int i = 0; i < pedidos.size(); i++) {
            Pedido p = pedidos.get(i);
            if (i < 3) {
                p.marcarEntregado();
            }
            guardar(p);
        }
    }

    private void crearLote(LocalDate inicio, String litros, int diasExtra, Integer botellas, String nota,
                           Insumo agua, Insumo flor, Insumo azucar, Insumo levadura) {
        BigDecimal l = new BigDecimal(litros);
        int estimadas = l.multiply(BigDecimal.valueOf(24)).divide(BigDecimal.valueOf(19), 0, RoundingMode.FLOOR).intValue();
        Lote lote = new Lote(inicio, l, DIAS_FERMENTACION, DIAS_REPOSO, estimadas, nota);
        if (diasExtra > 0) {
            lote.alargarFermentacion(diasExtra);
        }
        if (botellas != null) {
            lote.embotellar(lote.fechaListaEstimada(), botellas);
        }
        guardar(lote);

        BigDecimal factor = l.divide(BigDecimal.valueOf(19), 4, RoundingMode.HALF_UP);
        guardar(new LoteInsumo(lote, agua, escalar("18", factor)));
        guardar(new LoteInsumo(lote, flor, escalar("2", factor)));
        guardar(new LoteInsumo(lote, azucar, escalar("8", factor)));
        guardar(new LoteInsumo(lote, levadura, escalar("110", factor)));
    }

    private static BigDecimal escalar(String porMicroLote, BigDecimal factor) {
        return new BigDecimal(porMicroLote).multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private <T> T guardar(T entidad) {
        em.persist(entidad);
        return entidad;
    }
}
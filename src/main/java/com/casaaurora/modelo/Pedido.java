package com.casaaurora.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Pedido de botellas. El id define el orden de llegada. */
@Entity
@Table(name = "pedido", indexes = @Index(name = "idx_pedido_fecha_entrega", columnList = "fecha_entrega"))
@PoliticaAcceso({"pedido_lectura_solo_admin", "pedido_escritura_solo_admin"})
public class Pedido {

    /** L.300 por botella; L.280 desde 7 botellas (mayoreo). */
    public static BigDecimal precioPara(int cantidad) {
        return cantidad >= 7 ? new BigDecimal("280.00") : new BigDecimal("300.00");
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Column(nullable = false)
    private boolean entregado;

    protected Pedido() {
    }

    public Pedido(Cliente cliente, int cantidad, String canal, LocalDate fechaPedido, LocalDate fechaEntrega) {
        this.cliente = cliente;
        this.cantidad = cantidad;
        this.precioUnitario = precioPara(cantidad);
        this.canal = canal;
        this.fechaPedido = fechaPedido;
        this.fechaEntrega = fechaEntrega;
    }

    public void marcarEntregado() {
        this.entregado = true;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public String getCanal() { return canal; }
    public LocalDate getFechaPedido() { return fechaPedido; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public boolean isEntregado() { return entregado; }
}
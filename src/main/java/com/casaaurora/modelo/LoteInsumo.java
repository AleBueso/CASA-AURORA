package com.casaaurora.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Cantidad de cada insumo usada en un lote (la receta real de ese lote). */
@Entity
@Table(name = "lote_insumo")
@PoliticaAcceso({"lote_insumo_lectura_solo_admin", "lote_insumo_escritura_solo_admin"})
public class LoteInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cantidad;

    protected LoteInsumo() {
    }

    public LoteInsumo(Lote lote, Insumo insumo, BigDecimal cantidad) {
        this.lote = lote;
        this.insumo = insumo;
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public Lote getLote() { return lote; }
    public Insumo getInsumo() { return insumo; }
    public BigDecimal getCantidad() { return cantidad; }
}
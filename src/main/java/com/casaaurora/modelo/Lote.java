package com.casaaurora.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Lote de producción de vino de flor de Jamaica. */
@Entity
@Table(name = "lote", indexes = @Index(name = "idx_lote_fecha_inicio", columnList = "fecha_inicio"))
@PoliticaAcceso({"lote_lectura_solo_admin", "lote_escritura_solo_admin"})
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal litros;

    @Column(name = "dias_fermentacion", nullable = false)
    private int diasFermentacion;

    @Column(name = "dias_reposo", nullable = false)
    private int diasReposo;

    /** Días que se alargó la fermentación (por ejemplo, por la temperatura). */
    @Column(name = "dias_extra", nullable = false)
    private int diasExtra;

    @Column(name = "botellas_estimadas", nullable = false)
    private int botellasEstimadas;

    @Column(name = "botellas_obtenidas")
    private Integer botellasObtenidas;

    @Column(name = "fecha_embotellado")
    private LocalDate fechaEmbotellado;

    @Column(length = 300)
    private String observaciones;

    protected Lote() {
    }

    public Lote(LocalDate fechaInicio, BigDecimal litros, int diasFermentacion, int diasReposo,
                int botellasEstimadas, String observaciones) {
        this.fechaInicio = fechaInicio;
        this.litros = litros;
        this.diasFermentacion = diasFermentacion;
        this.diasReposo = diasReposo;
        this.botellasEstimadas = botellasEstimadas;
        this.observaciones = observaciones;
    }

    /** Fecha en que el lote debería estar listo para embotellar. */
    public LocalDate fechaListaEstimada() {
        return fechaInicio.plusDays((long) diasFermentacion + diasExtra + diasReposo);
    }

    public void alargarFermentacion(int dias) {
        this.diasExtra += dias;
    }

    public void embotellar(LocalDate fecha, int botellas) {
        this.fechaEmbotellado = fecha;
        this.botellasObtenidas = botellas;
    }

    public Long getId() { return id; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public BigDecimal getLitros() { return litros; }
    public int getDiasFermentacion() { return diasFermentacion; }
    public int getDiasReposo() { return diasReposo; }
    public int getDiasExtra() { return diasExtra; }
    public int getBotellasEstimadas() { return botellasEstimadas; }
    public Integer getBotellasObtenidas() { return botellasObtenidas; }
    public LocalDate getFechaEmbotellado() { return fechaEmbotellado; }
    public String getObservaciones() { return observaciones; }
}
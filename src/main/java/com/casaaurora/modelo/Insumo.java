package com.casaaurora.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Ingrediente o material que se usa en los lotes (flor de Jamaica, azúcar, agua, levadura). */
@Entity
@Table(name = "insumo")
@PoliticaAcceso({"insumo_lectura_solo_admin", "insumo_escritura_solo_admin"})
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String unidad;

    protected Insumo() {
    }

    public Insumo(String nombre, String unidad) {
        this.nombre = nombre;
        this.unidad = unidad;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
}
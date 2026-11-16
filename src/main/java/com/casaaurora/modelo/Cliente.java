package com.casaaurora.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Cliente que hace pedidos (por WhatsApp, redes, ferias o en persona). */
@Entity
@Table(name = "cliente", indexes = @Index(name = "idx_cliente_nombre", columnList = "nombre"))
@PoliticaAcceso({"cliente_lectura_solo_admin", "cliente_escritura_solo_admin"})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    @Column(name = "canal_preferido", length = 20)
    private String canalPreferido;

    protected Cliente() {
    }

    public Cliente(String nombre, String telefono, String canalPreferido) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.canalPreferido = canalPreferido;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCanalPreferido() { return canalPreferido; }
}
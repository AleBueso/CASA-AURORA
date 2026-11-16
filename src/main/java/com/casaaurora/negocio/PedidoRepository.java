package com.casaaurora.negocio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casaaurora.modelo.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** El id define el orden de llegada. */
    List<Pedido> findAllByOrderByIdAsc();
}
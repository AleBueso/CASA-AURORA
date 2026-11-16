package com.casaaurora.negocio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casaaurora.modelo.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findAllByOrderByFechaInicioDescIdDesc();
}
package com.casaaurora.negocio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.casaaurora.negocio.Vistas.LoteVista;
import com.casaaurora.negocio.Vistas.PedidoVista;
import com.casaaurora.negocio.Vistas.Resumen;

/** API privada del panel (requiere sesión con rol ADMIN, ver SecurityConfig). */
@RestController
@RequestMapping("/api")
public class NegocioController {

    private final NegocioService servicio;

    public NegocioController(NegocioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    public Resumen resumen() {
        return servicio.resumen();
    }

    @GetMapping("/lotes")
    public List<LoteVista> lotes() {
        return servicio.lotes();
    }

    @GetMapping("/pedidos")
    public List<PedidoVista> pedidos() {
        return servicio.pedidos();
    }
}
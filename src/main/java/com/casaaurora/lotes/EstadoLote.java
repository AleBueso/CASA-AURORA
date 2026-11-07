package com.casaaurora.lotes;

/**
 * Estados por los que pasa un lote de vino de flor de Jamaica.
 * FERMENTANDO: primeros 8 días (más los días extra por temperatura).
 * REPOSANDO: los 3 días siguientes.
 * LISTO: ya se puede embotellar.
 * EMBOTELLADO: ya se registraron las botellas obtenidas.
 */
public enum EstadoLote {
    FERMENTANDO,
    REPOSANDO,
    LISTO,
    EMBOTELLADO
}
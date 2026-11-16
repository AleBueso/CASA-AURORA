package com.casaaurora.esquema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.casaaurora.modelo.PoliticaAcceso;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.metamodel.EntityType;

/**
 * Exporta el esquema REAL de la base de datos a docs/db-export.json, leyéndolo con
 * DatabaseMetaData (tablas, columnas, llaves, índices, relaciones y cantidad de filas).
 *
 * Uso: .\mvnw spring-boot:run "-Dspring-boot.run.arguments=--app.exportar-esquema=true"
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.exportar-esquema", havingValue = "true")
public class ExportadorEsquema implements ApplicationRunner {

    private static final Path DESTINO = Path.of("docs", "db-export.json");

    private final DataSource dataSource;
    private final EntityManagerFactory emf;
    private final ConfigurableApplicationContext contexto;

    public ExportadorEsquema(DataSource dataSource, EntityManagerFactory emf, ConfigurableApplicationContext contexto) {
        this.dataSource = dataSource;
        this.emf = emf;
        this.contexto = contexto;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String json = exportar();
        Files.createDirectories(DESTINO.getParent());
        Files.writeString(DESTINO, json, StandardCharsets.UTF_8);
        System.out.println();
        System.out.println("=== Esquema exportado en " + DESTINO.toAbsolutePath() + " ===");
        System.out.println(resumen);
        System.exit(SpringApplication.exit(contexto, () -> 0));
    }

    private String resumen = "";

    String exportar() throws SQLException, IOException {
        Map<String, String[]> politicas = politicasPorTabla();
        Map<String, Long> filasPorTabla = filasPorTabla();
        StringBuilder sb = new StringBuilder();
        int totalTablas = 0, totalRelaciones = 0, totalIndices = 0, conDatos = 0, conPoliticas = 0;

        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            String esquema = c.getSchema();

            List<String> tablas = new ArrayList<>();
            try (ResultSet rs = md.getTables(null, esquema, "%", new String[] {"TABLE", "BASE TABLE"})) {
                while (rs.next()) {
                    tablas.add(rs.getString("TABLE_NAME"));
                }
            }
            tablas.sort(String::compareToIgnoreCase);

            sb.append("{\n");
            sb.append("  \"generado_at\": ").append(texto(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())).append(",\n");
            sb.append("  \"motor\": ").append(texto(md.getDatabaseProductName().toLowerCase(Locale.ROOT))).append(",\n");
            sb.append("  \"tablas\": [\n");

            for (int t = 0; t < tablas.size(); t++) {
                String tabla = tablas.get(t);
                String nombre = tabla.toLowerCase(Locale.ROOT);

                Set<String> pks = new LinkedHashSet<>();
                try (ResultSet rs = md.getPrimaryKeys(null, esquema, tabla)) {
                    while (rs.next()) {
                        pks.add(rs.getString("COLUMN_NAME"));
                    }
                }

                List<String> columnas = new ArrayList<>();
                try (ResultSet rs = md.getColumns(null, esquema, tabla, "%")) {
                    while (rs.next()) {
                        String col = rs.getString("COLUMN_NAME");
                        columnas.add("{\"nombre\":" + texto(col.toLowerCase(Locale.ROOT))
                                + ",\"tipo\":" + texto(rs.getString("TYPE_NAME").toLowerCase(Locale.ROOT))
                                + (pks.contains(col) ? ",\"pk\":true" : "")
                                + ",\"nulo\":" + "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")) + "}");
                    }
                }

                Set<String> indices = new LinkedHashSet<>();
                try (ResultSet rs = md.getIndexInfo(null, esquema, tabla, false, false)) {
                    while (rs.next()) {
                        String idx = rs.getString("INDEX_NAME");
                        if (idx != null && idx.toLowerCase(Locale.ROOT).startsWith("idx_")) {
                            indices.add(texto(idx.toLowerCase(Locale.ROOT)));
                        }
                    }
                }

                List<String> relaciones = new ArrayList<>();
                try (ResultSet rs = md.getImportedKeys(null, esquema, tabla)) {
                    while (rs.next()) {
                        relaciones.add("{\"columna\":" + texto(rs.getString("FKCOLUMN_NAME").toLowerCase(Locale.ROOT))
                                + ",\"referencia\":" + texto((rs.getString("PKTABLE_NAME") + "." + rs.getString("PKCOLUMN_NAME")).toLowerCase(Locale.ROOT)) + "}");
                    }
                }

                long filas = filasPorTabla.getOrDefault(nombre, 0L);

                List<String> pol = new ArrayList<>();
                for (String p : politicas.getOrDefault(nombre, new String[0])) {
                    pol.add(texto(p));
                }

                totalTablas++;
                totalRelaciones += relaciones.size();
                totalIndices += indices.size();
                if (filas > 0) conDatos++;
                if (!pol.isEmpty()) conPoliticas++;

                sb.append("    {\n");
                sb.append("      \"nombre\": ").append(texto(nombre)).append(",\n");
                sb.append("      \"filas\": ").append(filas).append(",\n");
                sb.append("      \"columnas\": [\n        ").append(String.join(",\n        ", columnas)).append("\n      ],\n");
                sb.append("      \"indices\": [").append(String.join(", ", indices)).append("],\n");
                sb.append("      \"relaciones\": [").append(String.join(", ", relaciones)).append("],\n");
                sb.append("      \"politicas_rls\": [").append(String.join(", ", pol)).append("]\n");
                sb.append("    }").append(t < tablas.size() - 1 ? "," : "").append("\n");
            }
            sb.append("  ]\n}\n");
        }

        resumen = "Tablas: " + totalTablas + " | Relaciones: " + totalRelaciones + " | Indices: " + totalIndices
                + " | Tablas con datos: " + conDatos + " | Tablas con politicas: " + conPoliticas;
        return sb.toString();
    }

    /**
     * Cuenta las filas de cada tabla con consultas JPA Criteria (sin armar SQL con texto),
     * para evitar cualquier riesgo de inyección SQL.
     */
    private Map<String, Long> filasPorTabla() {
        Map<String, Long> mapa = new HashMap<>();
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            for (EntityType<?> entidad : emf.getMetamodel().getEntities()) {
                Table tabla = entidad.getJavaType().getAnnotation(Table.class);
                if (tabla != null) {
                    CriteriaQuery<Long> consulta = cb.createQuery(Long.class);
                    consulta.select(cb.count(consulta.from(entidad.getJavaType())));
                    mapa.put(tabla.name().toLowerCase(Locale.ROOT), em.createQuery(consulta).getSingleResult());
                }
            }
        } finally {
            em.close();
        }
        return mapa;
    }

    /** Lee las políticas declaradas con @PoliticaAcceso en cada entidad JPA. */
    private Map<String, String[]> politicasPorTabla() {
        Map<String, String[]> mapa = new HashMap<>();
        for (EntityType<?> entidad : emf.getMetamodel().getEntities()) {
            Class<?> clase = entidad.getJavaType();
            Table tabla = clase.getAnnotation(Table.class);
            PoliticaAcceso politica = clase.getAnnotation(PoliticaAcceso.class);
            if (tabla != null && politica != null) {
                mapa.put(tabla.name().toLowerCase(Locale.ROOT), politica.value());
            }
        }
        return mapa;
    }

    private static String texto(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : valor.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(ch < 0x20 ? String.format("\\u%04x", (int) ch) : String.valueOf(ch));
            }
        }
        return sb.append('"').toString();
    }
}
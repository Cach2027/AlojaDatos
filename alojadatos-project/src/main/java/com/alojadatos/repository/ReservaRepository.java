package com.alojadatos.repository;

import com.alojadatos.model.Reserva;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** CRUD de la tabla "reserva" con JdbcTemplate: aquí vive TODO el SQL. */
@Repository
public class ReservaRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public ReservaRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    private static final String COLUMNAS =
            "id, huesped_id, fecha_inicio, fecha_fin, estado";

    // El RowMapper usa los nombres de las COLUMNAS (snake_case)
    private static final RowMapper<Reserva> MAPPER = (rs, rowNum) -> {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setHuespedId(rs.getInt("huesped_id"));
        r.setFechaInicio(rs.getObject("fecha_inicio", LocalDate.class));
        r.setFechaFin(rs.getObject("fecha_fin", LocalDate.class));
        r.setEstado(rs.getString("estado"));
        return r;
    };

    // ── READ ───────────────────────────────────────────────────────────

    public List<Reserva> findAll() {
        return jdbc.query("SELECT " + COLUMNAS + " FROM reserva ORDER BY id", MAPPER);
    }

    public Optional<Reserva> findById(int id) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM reserva WHERE id = ?", MAPPER, id)
                   .stream()
                   .findFirst();
    }

    // ── CREATE ─────────────────────────────────────────────────────────

    /** INSERT y devuelve la reserva con el id que generó PostgreSQL. */
    public Reserva save(Reserva reserva) {
        // Si no envían estado, se usa el mismo valor por defecto de la tabla
        if (reserva.getEstado() == null) {
            reserva.setEstado("Pendiente");
        }
        String sql = """
                INSERT INTO reserva (huesped_id, fecha_inicio, fecha_fin, estado)
                VALUES (:huespedId, :fechaInicio, :fechaFin, :estado)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("huespedId",   reserva.getHuespedId())
                .addValue("fechaInicio", reserva.getFechaInicio())
                .addValue("fechaFin",    reserva.getFechaFin())
                .addValue("estado",      reserva.getEstado());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbc.update(sql, params, keyHolder, new String[]{"id"});
        reserva.setId(keyHolder.getKey().intValue());
        return reserva;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────

    /** true si actualizó una fila; false si el id no existe. */
    public boolean update(int id, Reserva reserva) {
        String sql = """
                UPDATE reserva
                   SET huesped_id = :huespedId, fecha_inicio = :fechaInicio,
                       fecha_fin = :fechaFin, estado = :estado
                 WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("huespedId",   reserva.getHuespedId())
                .addValue("fechaInicio", reserva.getFechaInicio())
                .addValue("fechaFin",    reserva.getFechaFin())
                .addValue("estado",      reserva.getEstado())
                .addValue("id",          id);
        return namedJdbc.update(sql, params) > 0;
    }

    // ── DELETE ─────────────────────────────────────────────────────────

    /** true si borró una fila; false si el id no existe. */
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM reserva WHERE id = ?", id) > 0;
    }

    // ── RETO: procedimiento almacenado (solo si cargaron el 03) ───────

    /**
     * CALL sp_registrar_reserva: valida disponibilidad, capacidad, bloqueos, etc.
     * Si algo falla, el RAISE EXCEPTION (P0001) sale como 400 por ManejadorErrores.
     * El INOUT p_reserva_id vuelve como una fila con el id creado.
     */
    public Integer registrarConProcedimiento(int huespedId, int hotelId, int numeroHabitacion,
                                             LocalDate inicio, LocalDate fin, int numeroHuespedes) {
        return jdbc.queryForObject(
                "CALL sp_registrar_reserva(?, ?, ?, ?, ?, ?, NULL::int)",
                Integer.class,
                huespedId, hotelId, numeroHabitacion, inicio, fin, numeroHuespedes);
    }
}
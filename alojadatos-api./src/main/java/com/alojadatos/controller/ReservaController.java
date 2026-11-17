package com.alojadatos.controller;

import com.alojadatos.model.Reserva;
import com.alojadatos.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Endpoints REST de "reservas":
 *   GET    /reservas               → lista todas          200
 *   GET    /reservas/{id}          → una por id           200 · 404
 *   POST   /reservas      + JSON   → crea                 201 · 409
 *   PUT    /reservas/{id} + JSON   → actualiza            200 · 404 · 409
 *   DELETE /reservas/{id}          → elimina              204 · 404
 *   POST   /reservas/registrar     → reto (procedimiento) 201 · 400
 */
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaRepository repo;

    public ReservaController(ReservaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Reserva> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> buscar(@PathVariable("id") int id) {
        return repo.findById(id)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Reserva> crear(@RequestBody Reserva reserva) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(reserva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable("id") int id,
                                           @RequestBody Reserva reserva) {
        return repo.update(id, reserva)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
        return repo.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // ── RETO ───────────────────────────────────────────────────────────

    /** Cuerpo del POST /reservas/registrar (record: no es una tabla). */
    public record SolicitudReserva(int huespedId, int hotelId, int numeroHabitacion,
                                   LocalDate fechaInicio, LocalDate fechaFin,
                                   int numeroHuespedes) { }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Integer>> registrar(@RequestBody SolicitudReserva s) {
        Integer id = repo.registrarConProcedimiento(s.huespedId(), s.hotelId(),
                s.numeroHabitacion(), s.fechaInicio(), s.fechaFin(), s.numeroHuespedes());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }
}
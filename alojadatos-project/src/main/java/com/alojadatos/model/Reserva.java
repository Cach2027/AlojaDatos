package com.alojadatos.model;

import java.time.LocalDate;

/**
 * Una fila de la tabla "reserva".
 *
 *   id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
 *   huesped_id    INT NOT NULL → huesped(usuario_id)
 *   fecha_inicio  DATE NOT NULL
 *   fecha_fin     DATE NOT NULL, CHECK (fecha_fin > fecha_inicio)
 *   estado        VARCHAR(30) NOT NULL DEFAULT 'Pendiente'
 *                 (Pendiente, Confirmada, Cancelada, Finalizada)
 */
public class Reserva {

    private int       id;
    private int       huespedId;      // columna huesped_id
    private LocalDate fechaInicio;    // columna fecha_inicio
    private LocalDate fechaFin;       // columna fecha_fin
    private String    estado;

    public Reserva() { }

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public int getHuespedId()                       { return huespedId; }
    public void setHuespedId(int huespedId)         { this.huespedId = huespedId; }

    public LocalDate getFechaInicio()               { return fechaInicio; }
    public void setFechaInicio(LocalDate f)         { this.fechaInicio = f; }

    public LocalDate getFechaFin()                  { return fechaFin; }
    public void setFechaFin(LocalDate f)            { this.fechaFin = f; }

    public String getEstado()                       { return estado; }
    public void setEstado(String estado)            { this.estado = estado; }
}
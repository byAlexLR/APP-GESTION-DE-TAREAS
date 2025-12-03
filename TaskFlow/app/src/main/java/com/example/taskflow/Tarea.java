package com.example.taskflow;

import java.io.Serializable;

public class Tarea implements Serializable {
    private String titulo;
    private String fechaHora;
    private String descripcion;
    private String ubicacion;
    private boolean expanded;

    // Datos numéricos fecha
    private int dia, mes, anio;
    private int horaInicio, minInicio;
    private String amPmInicio;
    private int horaFin, minFin;
    private String amPmFin;

    // --- NUEVO: Datos de notificación ---
    private String notifCantidad;
    private String notifUnidad;

    // --- CONSTRUCTOR COMPLETO (15 DATOS) ---
    // Este es el que está buscando tu CrearTareaActivity y no encontraba
    public Tarea(String titulo, String fechaHora, String descripcion, String ubicacion,
                 int dia, int mes, int anio,
                 int horaInicio, int minInicio, String amPmInicio,
                 int horaFin, int minFin, String amPmFin,
                 String notifCantidad, String notifUnidad) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.expanded = false;

        this.dia = dia; this.mes = mes; this.anio = anio;
        this.horaInicio = horaInicio; this.minInicio = minInicio; this.amPmInicio = amPmInicio;
        this.horaFin = horaFin; this.minFin = minFin; this.amPmFin = amPmFin;

        this.notifCantidad = notifCantidad;
        this.notifUnidad = notifUnidad;
    }

    // Constructor SIMPLE (Para evitar errores en otras partes del código)
    public Tarea(String titulo, String fechaHora) {
        this(titulo, fechaHora, "", "", 0, 0, 0, 0, 0, "AM", 0, 0, "AM", "30", "Minutos antes");
    }

    // Getters
    public String getTitulo() { return titulo; }
    public String getFechaHora() { return fechaHora; }
    public String getDescripcion() { return descripcion; }
    public String getUbicacion() { return ubicacion; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }

    public int getDia() { return dia; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
    public int getHoraInicio() { return horaInicio; }
    public int getMinInicio() { return minInicio; }
    public String getAmPmInicio() { return amPmInicio; }
    public int getHoraFin() { return horaFin; }
    public int getMinFin() { return minFin; }
    public String getAmPmFin() { return amPmFin; }

    // Getters nuevos
    public String getNotifCantidad() { return notifCantidad; }
    public String getNotifUnidad() { return notifUnidad; }
}
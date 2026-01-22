package com.example.taskflow;

// Importa las líbrerias necesarias
import java.io.Serializable;

public class Tarea implements Serializable {
    // Datos básicos
    private final String titulo;
    private final String fechaHora;
    private final String descripcion;
    private final String ubicacion;

    // Estado de la tarea y expandir tarea
    private boolean expanded;
    private boolean completada;

    // Datos de la fecha
    private final int dia;
    private final int mes;
    private final int anio;
    private final int horaInicio;
    private final int minInicio;
    private final String amPmInicio;
    private final int horaFin, minFin;
    private final String amPmFin;

    // Datos de notificaciones
    private final String notifCantidad;
    private final String notifUnidad;

    // Constructor de la clase
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
        this.completada = false;

        this.dia = dia; this.mes = mes; this.anio = anio;
        this.horaInicio = horaInicio; this.minInicio = minInicio; this.amPmInicio = amPmInicio;
        this.horaFin = horaFin; this.minFin = minFin; this.amPmFin = amPmFin;

        this.notifCantidad = notifCantidad;
        this.notifUnidad = notifUnidad;
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public String getFechaHora() { return fechaHora; }
    public String getDescripcion() { return descripcion; }
    public String getUbicacion() { return ubicacion; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public int getDia() { return dia; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
    public int getHoraInicio() { return horaInicio; }
    public int getMinInicio() { return minInicio; }
    public String getAmPmInicio() { return amPmInicio; }
    public int getHoraFin() { return horaFin; }
    public int getMinFin() { return minFin; }
    public String getAmPmFin() { return amPmFin; }
    public String getNotifCantidad() { return notifCantidad; }
    public String getNotifUnidad() { return notifUnidad; }

    // Método auxiliar para convertir hora inicio a formato 0-23
    public int getHoraInicio24() {
        if (amPmInicio.equals("AM")) {
            if (horaInicio == 12) return 0; // 12 AM es 00:00
            return horaInicio;
        } else { // PM
            if (horaInicio == 12) return 12; // 12 PM es 12:00
            return horaInicio + 12; // 1 PM es 13:00
        }
    }

    // Método auxiliar para convertir hora fin a formato 0-23
    public int getHoraFin24() {
        if (amPmFin.equals("AM")) {
            if (horaFin == 12) return 0;
            return horaFin;
        } else { // PM
            if (horaFin == 12) return 12;
            return horaFin + 12;
        }
    }
}
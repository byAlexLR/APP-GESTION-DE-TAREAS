package com.example.taskflow;

import java.io.Serializable;

public class Tarea implements Serializable {
    private String titulo;
    private String fechaHora;
    private String descripcion;
    private String ubicacion;
    private boolean expanded;

    // Datos numéricos para edición
    private int dia, mes, anio;
    private int horaInicio, minInicio;
    private String amPmInicio;
    private int horaFin, minFin;
    private String amPmFin;

    // --- CONSTRUCTOR 1: EL COMPLETO (13 datos) ---
    public Tarea(String titulo, String fechaHora, String descripcion, String ubicacion,
                 int dia, int mes, int anio,
                 int horaInicio, int minInicio, String amPmInicio,
                 int horaFin, int minFin, String amPmFin) {
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.expanded = false;

        this.dia = dia; this.mes = mes; this.anio = anio;
        this.horaInicio = horaInicio; this.minInicio = minInicio; this.amPmInicio = amPmInicio;
        this.horaFin = horaFin; this.minFin = minFin; this.amPmFin = amPmFin;
    }

    // --- CONSTRUCTOR 2: INTERMEDIO (4 datos) ---
    public Tarea(String titulo, String fechaHora, String descripcion, String ubicacion) {
        // Llama al constructor completo con ceros en los datos numéricos
        this(titulo, fechaHora, descripcion, ubicacion, 0, 0, 0, 0, 0, "", 0, 0, "");
    }

    // --- CONSTRUCTOR 3: EL SIMPLE (2 datos) --- ¡ESTE ES EL QUE TE FALTABA!
    // Esto arregla tu error en MainActivity
    public Tarea(String titulo, String fechaHora) {
        // Llama al constructor intermedio poniendo descripción y ubicación vacías
        this(titulo, fechaHora, "", "");
    }

    // --- GETTERS Y SETTERS ---
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
}
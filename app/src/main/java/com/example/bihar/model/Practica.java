package com.example.bihar.model;

public class Practica {

    private String nombreEmpresa;
    private String provincia_es;
    private String provincia_eu;
    private String localidad_es;
    private String localidad_eu;
    private String horasTotales;
    private String salarioTotal;
    private String titulo;
    private String tareas;
    private String fechaInicio;
    private String fechaFin;

    public Practica(String nombreEmpresa, String provincia_es, String provincia_eu, String localidad_es,  String localidad_eu, String horasTotales, String salarioTotal, String titulo, String tareas, String fechaInicio, String fechaFin) {
        this.nombreEmpresa = nombreEmpresa;
        this.provincia_es = provincia_es;
        this.provincia_eu = provincia_eu;
        this.localidad_es = localidad_es;
        this.localidad_eu = localidad_eu;
        this.horasTotales = horasTotales;
        this.salarioTotal = salarioTotal;
        this.titulo = titulo;
        this.tareas = tareas;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getNombreEmpresa() {
        return this.nombreEmpresa;
    }

    public String getProvincia_es() {
        return this.provincia_es;
    }

    public String getProvincia_eu() {
        return this.provincia_eu;
    }

    public String getLocalidad_es() {
        return this.localidad_es;
    }

    public String getLocalidad_eu() {
        return this.localidad_eu;
    }

    public String getHorasTotales() {
        return this.horasTotales;
    }

    public String getSalarioTotal() {
        return this.salarioTotal;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String getTareas() {
        return this.tareas;
    }

    public String getFechaInicio() {
        return this.fechaInicio;
    }

    public String getFechaFin() {
        return this.fechaFin;
    }

}
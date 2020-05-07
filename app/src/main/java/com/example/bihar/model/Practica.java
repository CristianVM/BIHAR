package com.example.bihar.model;

public class Practica {

    private String nombreEmpresa;
    private String provincia;
    private String localidad;
    private String horasTotales;
    private String salarioTotal;
    private String titulo;
    private String fechaInicio;
    private String fechaFin;

    public Practica(String nombreEmpresa, String provincia, String localidad, String horasTotales, String salarioTotal, String titulo, String fechaInicio, String fechaFin) {
        this.nombreEmpresa = nombreEmpresa;
        this.provincia = provincia;
        this.localidad = localidad;
        this.horasTotales = horasTotales;
        this.salarioTotal = salarioTotal;
        this.titulo = titulo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getNombreEmpresa() {
        return this.nombreEmpresa;
    }

    public String getProvincia() {
        return this.provincia;
    }

    public String getLocalidad() {
        return this.localidad;
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

    public String getFechaInicio() {
        return this.fechaInicio;
    }

    public String getFechaFin() {
        return this.fechaFin;
    }

}
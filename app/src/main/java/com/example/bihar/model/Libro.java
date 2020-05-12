package com.example.bihar.model;

public class Libro {

    private String titulo;
    private String autor;
    private String descripcion;
    private String editorial;
    private String fecha;
    private String tema;
    private String temaEuskera;

    public Libro(String titulo, String autor, String descripcion, String editorial, String fecha, String tema,
                 String temaEuskera) {
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.editorial = editorial;
        this.fecha = fecha;
        this.tema = tema;
        this.temaEuskera = temaEuskera;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEditorial() {
        return editorial;
    }

    public String getFecha() {
        return fecha;
    }

    public String getTema() {
        return tema;
    }

    public String getTemaEuskera() {
        return temaEuskera;
    }
}

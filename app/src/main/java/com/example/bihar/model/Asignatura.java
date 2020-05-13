package com.example.bihar.model;

/**
 * Objeto que representa una Asignatura
 */
public class Asignatura {

    private String nombreAsignatura;
    private double calificacionOrd;
    private int convocatoria;
    private String tipo;
    private int anyo;
    private int curso;

    public Asignatura(String pNombre, double pCalificacionOrd, int pConv, String pTipo, int pAnyo, int pCurso){
        nombreAsignatura = pNombre;
        calificacionOrd = pCalificacionOrd;
        convocatoria = pConv;
        tipo = pTipo;
        anyo = pAnyo;
        curso = pCurso;
    }

    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public double getCalificacionOrd() {
        return calificacionOrd;
    }

    public int getConvocatoria() {
        return convocatoria;
    }

    public String getTipo() {
        return tipo;
    }

    public int getAnyo() {
        return anyo;
    }

    public int getCurso() {
        return curso;
    }
}

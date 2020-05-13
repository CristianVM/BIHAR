package com.example.bihar.model;


import java.util.ArrayList;

/**
 * Objeto que gestiona las tutorias que pueden llegar a haber en un día en concreto
 */
public class FechaTutoria {
    private String fecha;
    private ArrayList<Tutoria> tutorias;

    FechaTutoria(int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin, int pEstado) {
        fecha = pFecha;

        tutorias = new ArrayList<>();
        tutorias.add(new Tutoria(pIdTutoria, pHoraInicio + " - " + pHoraFin, pEstado));
    }

    void anadirHora(int pIdTutoria, String pHoraInicio, String pHoraFin, int pEstado) {
        tutorias.add(new Tutoria(pIdTutoria, pHoraInicio + " - " + pHoraFin, pEstado));
    }


    public String getFecha() {
        return fecha;
    }

    public ArrayList<Tutoria> getHoras() {
        return tutorias;
    }
}


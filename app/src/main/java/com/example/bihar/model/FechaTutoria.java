package com.example.bihar.model;

import java.util.ArrayList;

public class FechaTutoria {
    private String fecha;
    private ArrayList<Tutoria> tutorias;

    FechaTutoria(int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin, int pEstado){
        fecha = pFecha;

        tutorias = new ArrayList<>();
        tutorias.add(new Tutoria(pIdTutoria,pHoraFin +" - "+pHoraFin, pEstado));
    }

    void anadirHora(int pIdTutoria, String pHoraInicio, String pHoraFin, int pEstado){
        tutorias.add(new Tutoria(pIdTutoria,pHoraFin +" - "+pHoraFin, pEstado));
    }


    public String getFecha() {
        return fecha;
    }

    public ArrayList<Tutoria> getHoras(){
        return tutorias;
    }
}


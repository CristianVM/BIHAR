package com.example.bihar.model;

import java.util.ArrayList;

public class FechaTutoria {
    private String fecha;
    private ArrayList<Tutoria> tutorias;

    FechaTutoria(int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin, boolean pReservado){
        fecha = pFecha;

        tutorias = new ArrayList<>();
        tutorias.add(new Tutoria(pIdTutoria,pHoraFin +" - "+pHoraFin, pReservado));
    }

    void anadirHora(int pIdTutoria, String pHoraInicio, String pHoraFin, boolean pReservado){
        tutorias.add(new Tutoria(pIdTutoria,pHoraFin +" - "+pHoraFin, pReservado));
    }


    public String getFecha() {
        return fecha;
    }

    public ArrayList<Tutoria> getHoras(){
        return tutorias;
    }
}


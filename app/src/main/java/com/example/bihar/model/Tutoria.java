package com.example.bihar.model;

import java.util.HashMap;

public class Tutoria {
    private String fecha;
    private HashMap<Integer, String> horas;

    Tutoria(int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin){
        fecha = pFecha;

        horas = new HashMap<>();
        horas.put(pIdTutoria, pHoraInicio + " - " + pHoraFin);
    }

    void anadirHora(int pIdTutoria, String pHoraInicio, String pHoraFin){
        horas.put(pIdTutoria,pHoraInicio + " - " + pHoraFin);
    }


    public String getFecha() {
        return fecha;
    }

    public HashMap<Integer, String> getHoras(){
        return horas;
    }
}

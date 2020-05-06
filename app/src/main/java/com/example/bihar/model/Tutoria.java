package com.example.bihar.model;

import java.util.ArrayList;

public class Tutoria {
    private int idTutoria;
    private String fecha;
    private ArrayList<String> horas;

    public Tutoria (int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin){
        idTutoria = pIdTutoria;
        fecha = pFecha;

        horas = new ArrayList<String>();
        horas.add(pHoraInicio + " - " + pHoraFin);
    }

    public void anadirHora(String pHoraInicio, String pHoraFin){
        horas.add(pHoraInicio + " - " + pHoraFin);
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public String getFecha() {
        return fecha;
    }

    public String[] getHoras(){
        return horas.toArray(new String[0]);
    }
}

package com.example.bihar.model;

public class Tutoria{
    private int idTutoria;
    private String hora;
    private boolean reservado;

    public Tutoria (int pId, String pHora, boolean pReservado){
        idTutoria = pId;
        hora = pHora;
        reservado = pReservado;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public String getHora() {
        return hora;
    }

    public boolean isReservado() {
        return reservado;
    }

    public void setReservado(boolean b) {
        reservado = b;
    }
}

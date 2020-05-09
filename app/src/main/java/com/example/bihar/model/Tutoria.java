package com.example.bihar.model;

public class Tutoria{
    private int idTutoria;
    private String hora;
    private int estado;

    public Tutoria (int pId, String pHora, int pEstado){
        idTutoria = pId;
        hora = pHora;
        estado = pEstado;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public String getHora() {
        return hora;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int b) {
        estado = b;
    }
}

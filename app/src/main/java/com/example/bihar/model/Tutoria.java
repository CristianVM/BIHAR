package com.example.bihar.model;

/**
 * Objeto que representa una Tutoria, con el id, la hora y el estado {0(Pendiente), 1(Aceptado), 2(Rechazado), -1(Sin solicitar)}
 */
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

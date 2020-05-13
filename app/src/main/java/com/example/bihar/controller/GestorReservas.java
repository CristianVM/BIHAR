package com.example.bihar.controller;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase singleton que gestiona las reservas de tutorias recogidas de la base de datos
 */
public class GestorReservas {

    private static GestorReservas mGestorReservas;
    private ArrayList<Reserva> reservas;

    private GestorReservas() {
        reservas = new ArrayList<>();
    }

    public static GestorReservas getGestorReservas() {
        if (mGestorReservas == null)
            mGestorReservas = new GestorReservas();
        return mGestorReservas;
    }

    public void anadirReserva(int idTutoria, String idPersona, String fecha, int estado, String msg, String nombreCompleto) {
        reservas.add(new Reserva(idTutoria, idPersona, fecha, estado, msg, nombreCompleto));
    }

    public List<Integer> getIndices(int estado) {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < reservas.size(); i++) {
            Reserva r = reservas.get(i);
            if (r.getEstado() == estado)
                indices.add(i);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            indices.sort((o1, o2) -> reservas.get(o1).getFecha().compareTo(reservas.get(o2).getFecha()));
        }

        return indices;
    }

    public void setEstado(int index, int estado) {
        reservas.get(index).setEstado(estado);
    }

    public int getIdTutoria(int index) {
        return reservas.get(index).getIdTutoria();
    }

    public String getIdPersona(int index) {
        return reservas.get(index).getIdPersona();
    }

    public String getFecha(int index) {
        return reservas.get(index).getFecha();
    }

    public int getEstado(int index) {
        return reservas.get(index).getEstado();
    }

    public String getMsg(int index) {
        return reservas.get(index).getMsg();
    }

    public String getNombreCompleto(int index) {
        return reservas.get(index).getNombreCompleto();
    }

    public void borrarReservas() {
        reservas.clear();
    }
}

class Reserva {

    private int idTutoria;
    private String idPersona;
    private String fecha;
    private int estado;
    private String msg;
    private String nombreCompleto;

    public Reserva(int idTutoria, String idPersona, String fecha, int estado, String msg, String nombreCompleto) {
        this.idTutoria = idTutoria;
        this.idPersona = idPersona;
        this.fecha = fecha;
        this.estado = estado;
        this.msg = msg;
        this.nombreCompleto = nombreCompleto;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public String getFecha() {
        return fecha;
    }

    public int getEstado() {
        return estado;
    }

    public String getMsg() {
        return msg;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
}

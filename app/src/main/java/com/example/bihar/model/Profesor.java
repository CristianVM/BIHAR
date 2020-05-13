package com.example.bihar.model;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Clase que representa a un profesor. Gestiona las asignaturas que imparte y las tutorías que tiene disponibles
 */
public class Profesor {

    private static final int LONGITUD_MAXIMA = 65;

    private Uri uriFoto;
    private String nombreCompleto;
    private String departamento;
    private String despacho;
    private String nombreCentro;
    private ArrayList<FechaTutoria> fechaTutorias;
    private Map<Integer, String> asignaturas;

    public Profesor(String pNombreCompleto, int pIdAsignatura, String pNombreAsignatura) {
        fechaTutorias = new ArrayList<>();
        asignaturas = new HashMap<>();
        nombreCompleto = pNombreCompleto;
        asignaturas.put(pIdAsignatura, pNombreAsignatura);
    }

    public void setFoto(Uri foto) {
        uriFoto = foto;
    }

    public Uri getFoto() {
        return uriFoto;
    }

    public void anadirAsignatura(int pIdAsignatura, String pNombreAsignatura) {
        if (!asignaturas.containsKey(pIdAsignatura)) {
            asignaturas.put(pIdAsignatura, pNombreAsignatura);
        }
    }

    public String getAsignaturas() {
        String cadena = "";

        Collection<String> collection = asignaturas.values();
        Iterator<String> itr = collection.iterator();

        while (itr.hasNext()) {
            String asignatura = itr.next();
            cadena += (asignatura) + (itr.hasNext() ? " | " : "");
        }


        if (cadena.length() > LONGITUD_MAXIMA) {
            StringTokenizer token = new StringTokenizer(cadena, "|");
            cadena = "";
            Log.i("tokens", String.valueOf(token.hasMoreElements()));
            while (token.hasMoreElements()) {
                cadena += getAcronimo(token.nextToken()) + (token.hasMoreElements() ? " | " : "");
            }

        }

        return cadena;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    private String getAcronimo(String pAsignatura) {
        return pAsignatura.replaceAll("[^A-Z]", "");
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getDespacho() {
        return despacho;
    }

    public String getNombreCentro() {
        return nombreCentro;
    }

    public FechaTutoria[] getFechaTutorias() {
        return fechaTutorias.toArray(new FechaTutoria[0]);
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public void setDespacho(String despacho) {
        this.despacho = despacho;
    }

    public void setNombreCentro(String nombreCentro) {
        this.nombreCentro = nombreCentro;
    }

    public void anadirTutoria(int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin, int pEstado) {
        for (FechaTutoria fechaTutoria : fechaTutorias) {
            if (fechaTutoria.getFecha().equals(pFecha)) {
                fechaTutoria.anadirHora(pIdTutoria, pHoraInicio, pHoraFin, pEstado);
                return;
            }
        }
        this.fechaTutorias.add(new FechaTutoria(pIdTutoria, pFecha, pHoraInicio, pHoraFin, pEstado));
    }


    public void limpiarTutorias() {
        fechaTutorias.clear();
    }
}

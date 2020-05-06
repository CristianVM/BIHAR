package com.example.bihar.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class Profesor {

    private static final int LONGITUD_MAXIMA = 75;

    private String nombreCompleto;
    private String departamento;
    private String despacho;
    private String nombreCentro;
    private ArrayList<Tutoria> tutorias;
    private Map<Integer, String> asignaturas;

    public Profesor(String pNombreCompleto, int pIdAsignatura, String pNombreAsignatura){
        tutorias = new ArrayList<>();
        asignaturas = new HashMap<>();
        nombreCompleto = pNombreCompleto;
        asignaturas.put(pIdAsignatura,pNombreAsignatura);
    }

    public void anadirAsignatura(int pIdAsignatura, String pNombreAsignatura){
        if(!asignaturas.containsKey(pIdAsignatura)){
            asignaturas.put(pIdAsignatura,pNombreAsignatura);
        }
    }

    public String getAsignaturas(){
        String cadena = "";

        Collection<String> collection = asignaturas.values();
        Iterator<String> itr = collection.iterator();

        while (itr.hasNext()){
            String asignatura = itr.next();
            cadena += (asignatura) + (itr.hasNext() ? " | " : "");
        }


        if(cadena.length() > LONGITUD_MAXIMA){
            StringTokenizer token = new StringTokenizer(cadena, "|");
            cadena = "";
            Log.i("tokens",String.valueOf(token.hasMoreElements()));
            while (token.hasMoreElements()){
                cadena += getAcronimo(token.nextToken()) + (token.hasMoreElements()?" | ":"");
            }

        }

        return cadena;
    }

    public String getNombreCompleto(){
        return nombreCompleto;
    }

    private String getAcronimo(String pAsignatura){
        return pAsignatura.replaceAll("[^A-Z]","");
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

    public Tutoria[] getTutorias() {
        return tutorias.toArray(new Tutoria[0]);
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

    public void anadirTutoria(int pIdTutoria, String pFecha, String pHoraInicio, String pHoraFin){
        for(Tutoria tutoria: tutorias){
            if(tutoria.getFecha().equals(pFecha)){
                tutoria.anadirHora(pIdTutoria, pHoraInicio, pHoraFin);
                return;
            }
        }
        this.tutorias.add(new Tutoria(pIdTutoria, pFecha, pHoraInicio, pHoraFin));
    }

    public void limpiarTutorias(){
        tutorias.clear();
    }
}

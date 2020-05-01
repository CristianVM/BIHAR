package com.example.bihar.model;

import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class Profesor {

    private static final int LONGITUD_MAXIMA = 75;

    private String nombreCompleto;
    private Map<Integer, String> asignaturas;

    public Profesor(String pNombreCompleto, int pIdAsignatura, String pNombreAsignatura){
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

}

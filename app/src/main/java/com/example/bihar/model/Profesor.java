package com.example.bihar.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Profesor {

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
        StringBuilder cadena = new StringBuilder();

        Collection<String> collection = asignaturas.values();
        Iterator<String> itr = collection.iterator();

        while (itr.hasNext()){
            cadena.append(itr.next()).append(itr.hasNext() ? " | " : "");
        }

        return cadena.toString();
    }

    public String getNombreCompleto(){
        return nombreCompleto;
    }

}

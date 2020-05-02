package com.example.bihar.controller;

import com.example.bihar.model.Profesor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gestor que almacena todos los datos de los profesores, en caso que no haya algún dato guardado
 * lo pedirá de la Base de Datos
 */
public class GestorProfesores {

    private static GestorProfesores mGestorProfesores;

    private Map<String, Profesor> profesores;

    private GestorProfesores(){
        profesores = new HashMap<>();
    }

    public static GestorProfesores getGestorProfesores(){
        if(mGestorProfesores == null)
            mGestorProfesores = new GestorProfesores();
        return mGestorProfesores;
    }

    public void anadirProfesor(String idPersona, String nombreCompleto, int idAsignatura, String nombreAsignatura){
        if(profesores.containsKey(idPersona)){
            profesores.get(idPersona).anadirAsignatura(idAsignatura,nombreAsignatura);
        }else{
            profesores.put(idPersona, new Profesor(nombreCompleto,idAsignatura,nombreAsignatura));
        }
    }

    public List<String> getIds(String s){
        Set<String> ids = profesores.keySet();
        List<String> filtrado = new ArrayList<>();
        for(String id: ids){
            String nombre = profesores.get(id).getNombreCompleto();
            if(nombre.toLowerCase().contains(s.toLowerCase())){
                filtrado.add(id);
            }
        }

        return filtrado;
    }

    public Map<String, Profesor> getListaProfesores(){
        return profesores;
    }

    public void limpiar(){
        profesores.clear();
    }

}

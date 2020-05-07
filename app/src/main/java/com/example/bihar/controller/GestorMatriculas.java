package com.example.bihar.controller;

import android.content.Context;

import com.example.bihar.model.MatriculaAnios;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.HashMap;
import java.util.Map;

public class GestorMatriculas {

    private static GestorMatriculas gestorMatriculas;
    private Map<String, MatriculaAnios> matriculasPorUsuario;

    /**
     * Constructor
     */
    private GestorMatriculas(){
        matriculasPorUsuario = new HashMap<>();
    }

    /**
     * Se recoge el gestor de la matrícula
     * @return
     */
    public static GestorMatriculas gestorMatriculas(){
        if(gestorMatriculas==null){
            gestorMatriculas = new GestorMatriculas();
        }
        return gestorMatriculas;
    }

    /**
     * Se crea una matrícula con sus asignaturas
     * @param datos: las asignaturas
     * @param context: contexto de la aplicacion
     * @param idPersona: id de la persona
     */
    public void addMatriculas(String datos, Context context,String idPersona){
        JSONParser parser = new JSONParser();
        try{
            JSONArray jsonArray = (JSONArray) parser.parse(datos);
            MatriculaAnios matriculaAnios = new MatriculaAnios(context);
            matriculaAnios.crearMatricula(jsonArray);

            matriculasPorUsuario.put(idPersona,matriculaAnios);
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public MatriculaAnios getMatriculas(String idPersona){
        return matriculasPorUsuario.get(idPersona);
    }
}

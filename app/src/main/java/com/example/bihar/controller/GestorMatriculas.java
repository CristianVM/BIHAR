package com.example.bihar.controller;

import android.content.Context;

import com.example.bihar.model.MatriculaAnios;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GestorMatriculas {

    private static GestorMatriculas gestorMatriculas;
    private Map<String, MatriculaAnios> matriculasPorUsuario;

    private GestorMatriculas(){
        matriculasPorUsuario = new HashMap<>();
    }

    public static GestorMatriculas gestorMatriculas(){
        if(gestorMatriculas==null){
            gestorMatriculas = new GestorMatriculas();
        }
        return gestorMatriculas;
    }

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

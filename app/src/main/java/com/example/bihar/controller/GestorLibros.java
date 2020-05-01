package com.example.bihar.controller;

import com.example.bihar.model.Libro;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;

public class GestorLibros {

    private static GestorLibros gestorLibros;
    private List<Libro> listLibros;

    private GestorLibros(){
        listLibros = new ArrayList<Libro>();
    }

    public static GestorLibros getGestorLibros(){
        if(gestorLibros == null){
            gestorLibros = new GestorLibros();
        }
        return gestorLibros;
    }

    public void addLibro(String jsonString){
        JSONParser parser = new JSONParser();

        try{
            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            for(int i=0;i<jsonArray.size();i++){
                JSONObject json = (JSONObject) jsonArray.get(i);
                listLibros.add(new Libro(
                        (int) json.get("idLibro"),
                        (int) json.get("idCentro"),
                        (String) json.get("titulo"),
                        (String) json.get("autor"),
                        (String) json.get("descripcion"),
                        (String) json.get("editorial"),
                        (String) json.get("fecha"),
                        (String) json.get("tema")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    
}

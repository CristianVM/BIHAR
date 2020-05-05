package com.example.bihar.controller;

import com.example.bihar.model.Libro;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestorLibros {

    private static GestorLibros gestorLibros;
    private Map<String,Libro> libros;

    private GestorLibros(){
        libros = new HashMap<>();
    }

    public static GestorLibros getGestorLibros(){
        if(gestorLibros == null){
            gestorLibros = new GestorLibros();
        }
        return gestorLibros;
    }

    public Map<String,Libro> getLibros(){
        return libros;
    }

    public void addLibro(String jsonString){
        JSONParser parser = new JSONParser();
        try{
            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            for(int i=0;i<jsonArray.size();i++){
                JSONObject json = (JSONObject) jsonArray.get(i);
                if(!libros.containsKey((String) json.get("idLibro"))){
                    libros.put((String) json.get("idLibro"),new Libro(
                            (String) json.get("titulo"),
                            (String) json.get("autor"),
                            (String) json.get("descripcion"),
                            (String) json.get("editorial"),
                            (String) json.get("fecha"),
                            (String) json.get("tema")));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Libro getInfoLibro(String idLibro){
        return libros.get(idLibro);
    }

    public List<String> buscarLibro(String busqueda){
        Set<String> idLibros = libros.keySet();
        List<String> filtro = new ArrayList<>();

        for(String id: idLibros){
            String nombreLibro = libros.get(id).getTitulo();
            if(nombreLibro.toLowerCase().contains(busqueda.toLowerCase())){
                filtro.add(id);
            }
        }
        return filtro;
    }

    public List<String> filtrarLibro(String jsonFiltro){
        Set<String> idLibros = libros.keySet();
        List<String> filtro = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try{
            JSONObject json = (JSONObject) parser.parse(jsonFiltro);
            for(String id: idLibros){
                String tema = libros.get(id).getTema();
                if(tema.toLowerCase().equals(((String) json.get("filtroInformatica")).toLowerCase())
                        || tema.toLowerCase().equals(((String) json.get("filtroMedicina")).toLowerCase())
                        || tema.toLowerCase().equals(((String) json.get("filtroEconomia")).toLowerCase())){
                    filtro.add(id);
                }
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        return filtro;
    }
}

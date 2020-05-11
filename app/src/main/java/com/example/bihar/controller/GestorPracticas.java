package com.example.bihar.controller;

import android.content.Context;

import com.example.bihar.model.Libro;
import com.example.bihar.model.MatriculaAnios;
import com.example.bihar.model.Practica;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestorPracticas {

    private static GestorPracticas gestorPracticas;
    private Map<String, Practica> practicas;

    private GestorPracticas() {
        practicas = new HashMap<>();
    }

    public static GestorPracticas getGestorPracticas() {
        if (gestorPracticas == null) {
            gestorPracticas = new GestorPracticas();
        }
        return gestorPracticas;
    }

    public Map<String, Practica> getPracticas() {
        return practicas;
    }

    public void addPracticas(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                if (!practicas.containsKey((String) json.get("idOferta"))) {
                    practicas.put((String) json.get("idOferta"), new Practica(
                            (String) json.get("nombreEmpresa"),
                            (String) json.get("provincia_es"),
                            (String) json.get("provincia_eu"),
                            (String) json.get("localidad_es"),
                            (String) json.get("localidad_eu"),
                            (String) json.get("horasTotales"),
                            (String) json.get("salarioTotal"),
                            (String) json.get("titulo"),
                            (String) json.get("tareas"),
                            (String) json.get("fechaInicio"),
                            (String) json.get("fechaFin")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Practica getPractica(String idPractica) {
        return this.practicas.get(idPractica);
    }
}
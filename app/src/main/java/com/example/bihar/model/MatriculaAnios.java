package com.example.bihar.model;

import android.content.Context;

import com.example.bihar.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatriculaAnios {

    private Map<String, AlmacenajeMatricula> matriculas;
    private Context context;
    private ArrayList<String> anios;

    public MatriculaAnios(Context context) {
        this.context = context;
        matriculas = new HashMap<>();
        anios = new ArrayList<>();
    }

    public void crearMatricula(JSONArray jsonArray) {
        AlmacenajeMatricula almacenajeArrays =null;
        String anioEnJson="0";
        anios = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            // SI ES UN AÑO QUE NO HA APARECIDO SE AÑADE AL MAP LA MATRICULA ANTERIOR
            // Y SE CREA UN NUEVO ALMACEN DE MATRICULAS
            if(!anioEnJson.equals(jsonObject.get("anio"))){
                if(i>0){
                    matriculas.put(anioEnJson,almacenajeArrays);
                }
                almacenajeArrays = new AlmacenajeMatricula(context);
                anioEnJson = (String) jsonObject.get("anio");
                anios.add(anioEnJson);
            }
            //SE RECOGE LOS DATOS DEL JSON
            almacenajeArrays.setDato(jsonObject);
        }
        matriculas.put(anioEnJson,almacenajeArrays);
    }

    public Map<String,AlmacenajeMatricula> getMatriculas(){
        return matriculas;
    }

    public ArrayList<String> getAnios(){
        return anios;
    }

}

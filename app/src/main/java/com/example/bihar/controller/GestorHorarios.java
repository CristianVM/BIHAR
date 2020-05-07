package com.example.bihar.controller;

import android.content.Context;

import com.example.bihar.model.HorarioAsignatura;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorHorarios {

    private static GestorHorarios gestorHorarios;
    private Map<String, HorarioAsignatura> horarioAsignaturaMap;
    private SimpleDateFormat dateFormat;


    private GestorHorarios(){
        horarioAsignaturaMap = new HashMap<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public static GestorHorarios gestorHorarios(){
        if(gestorHorarios ==null){
            gestorHorarios = new GestorHorarios();
        }
        return gestorHorarios;
    }

    public void anadirHorarios(String jsonHorario, Context context){
        JSONParser parser = new JSONParser();
        try{
            JSONArray jsonArray = (JSONArray) parser.parse(jsonHorario);

            for(int i=0; i< jsonArray.size();i++){
                JSONObject json = (JSONObject) jsonArray.get(i);
                String idAsignatura = (String) json.get("idAsignatura");

                HorarioAsignatura horario = null;
                if(!horarioAsignaturaMap.containsKey(idAsignatura)){
                    horario = new HorarioAsignatura(context);
                }else{
                    horario = horarioAsignaturaMap.get(idAsignatura);
                }
                horario.addHorarioAsignatura(
                        (String) json.get("diaSemana"),
                        (String) json.get("semana"),
                        (String) json.get("horaInicio"),
                        (String) json.get("horaFin"),
                        (String) json.get("nombreAsignatura"));

                horarioAsignaturaMap.put(idAsignatura,horario);
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
    }
    public void limpiarHorarios(){
        horarioAsignaturaMap.clear();
    }

    public List<JSONObject> obtHorariosDelDia(Calendar calendar){

        List<JSONObject> resultado = new ArrayList<>();
        for(Map.Entry<String,HorarioAsignatura> datos: horarioAsignaturaMap.entrySet()){
            HorarioAsignatura horarioAsignatura = datos.getValue();

            JSONObject json = horarioAsignatura.horarioAsignatura(calendar,dateFormat);
            if(json.size()>0){
                resultado.add(json);
            }
        }
        return resultado;
    }
}

package com.example.bihar.controller;

import android.content.Context;
import android.util.Log;

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

    /**
     * Constructor
     */
    private GestorHorarios(){
        horarioAsignaturaMap = new HashMap<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    /**
     * Se obtiene el gestor de los horarios
     * @return: el gestor de horarios
     */
    public static GestorHorarios gestorHorarios(){
        if(gestorHorarios ==null){
            gestorHorarios = new GestorHorarios();
        }
        return gestorHorarios;
    }

    /**
     * Al obtener los datos en JSON de la base de datos, se va almacenando por asignaturas en el
     * hashmap
     * @param jsonHorario: json con los datos obtenido en String
     * @param context: el contexto de la aplicación
     */
    public void anadirHorarios(String jsonHorario, Context context){
        JSONParser parser = new JSONParser();
        try{
            JSONArray jsonArray = (JSONArray) parser.parse(jsonHorario);

            // RECORRE TODOS LOS HORARIOS
            for(int i=0; i< jsonArray.size();i++){
                JSONObject json = (JSONObject) jsonArray.get(i);
                String idAsignatura = (String) json.get("idAsignatura");

                HorarioAsignatura horario = null;
                // SE COMPRUEBA SI SE HA AÑADIDO AL HASHMAP
                if(!horarioAsignaturaMap.containsKey(idAsignatura)){
                    horario = new HorarioAsignatura(context);
                }else{
                    horario = horarioAsignaturaMap.get(idAsignatura);
                }
                //SE AÑADE EL HORARIO
                horario.addHorarioAsignatura(
                        (String) json.get("diaSemana"),
                        (String) json.get("semana"),
                        (String) json.get("horaInicio"),
                        (String) json.get("horaFin"),
                        (String) json.get("nombreAsignatura"));

                horarioAsignaturaMap.put(idAsignatura,horario);
            }
            Log.i("HORARIOS",horarioAsignaturaMap.size()+"");

        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Vacía el hashmap
     */
    public void limpiarHorarios(){
        horarioAsignaturaMap.clear();
    }

    /**
     * Se obtienen los horarios del mismo día que se le pasa por parámetros
     * @param calendar: el día que se quiera ver el horario
     * @return: la lista de asignaturas con sus horarios
     */
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

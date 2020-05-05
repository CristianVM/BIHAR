package com.example.bihar.model;

import android.content.Context;

import com.example.bihar.R;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class AlmacenajeMatricula {

    public ArrayList<String> asignaturaNombres;
    public ArrayList<String> asignaturaCursos;
    public ArrayList<String> asignaturasConvocatorias;
    public ArrayList<String> asignaturasOrdinarias;
    public ArrayList<String> asignaturasExtraordinarias;
    public Context context;

    /**
     * Constructor de AlmacenajeDatos
     */
    public AlmacenajeMatricula(Context context) {
        asignaturaNombres = new ArrayList<>();
        asignaturaCursos = new ArrayList<>();
        asignaturasConvocatorias = new ArrayList<>();
        asignaturasExtraordinarias = new ArrayList<>();
        asignaturasOrdinarias = new ArrayList<>();
        this.context = context;
    }

    /**
     * Se almacenan en las listas el nombre, el curso, la convocatoria y las notas de la asignatura
     *
     * @param jsonDato: el json con los datos de la asignatura
     */
    public void setDato(JSONObject jsonDato) {
        asignaturaNombres.add((String) jsonDato.get("nombreAsignatura"));
        asignaturaCursos.add((String) jsonDato.get("curso"));
        asignaturasConvocatorias.add((String) jsonDato.get("convocatoria"));

        String notaOrd = (String) jsonDato.get("notaOrdinaria");
        String notaExtrad = (String) jsonDato.get("notaExtraordinaria");
        setNotaTabla(notaOrd, notaExtrad);
    }

    /**
     * Con los datos obtenidos del json se añade la nota obtenida o un no presentado o aún no ha
     * realizado el examen
     *
     * @param notaOrd:    nota obtenida en la convocatoria ordinaria
     * @param notaExtrad: nota obtenida en la convocatoria extraordinaria
     */
    private void setNotaTabla(String notaOrd, String notaExtrad) {
        if (notaOrd.equals("-1")) {
            asignaturasOrdinarias.add("-");
        } else if (notaOrd.equals("-2")) {
            asignaturasOrdinarias.add(context.getResources().getString(R.string.matricula_notaNP));
        } else {
            asignaturasOrdinarias.add(notaOrd);
        }

        if (notaExtrad.equals("-1")) {
            asignaturasExtraordinarias.add("-");
        } else if (notaExtrad.equals("-2")) {
            asignaturasExtraordinarias.add(context.getResources().getString(R.string.matricula_notaNP));
        } else {
            asignaturasExtraordinarias.add(notaExtrad);
        }
    }
}

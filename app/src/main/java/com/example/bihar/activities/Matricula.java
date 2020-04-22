package com.example.bihar.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;

public class Matricula extends AppCompatActivity {

    private String[] asignaturaNombres;
    private int[] asignaturaCursos;
    private int[] asignaturasConvocatorias;
    private double[] asignaturasOrdinarias;
    private double[] asignaturasExtraordinarias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matricula);

        TextView txtSeleccionAnio = findViewById(R.id.matricula_seleccionAnio);

        //https://stackoverflow.com/questions/18003021/how-to-add-border-around-tablelayout
        asignaturaNombres = new String[2];
        asignaturaNombres[0] = "Gestion de proyectos";
        asignaturaCursos = new int[2];
        asignaturaCursos[0] = 1;
        asignaturasConvocatorias = new int[2];
        asignaturasConvocatorias[0] = 1;
        asignaturasOrdinarias = new double[2];
        asignaturasOrdinarias[0] = 5;
        asignaturasExtraordinarias = new double[2];
        asignaturasExtraordinarias[0] = 9;

        ListView asignaturas = (ListView) findViewById(R.id.matricula_lista);
        AdapterListaAsignaturasMatricula adapter = new AdapterListaAsignaturasMatricula(
                this, asignaturaNombres, asignaturaCursos, asignaturasConvocatorias, asignaturasOrdinarias, asignaturasExtraordinarias);
        asignaturas.setAdapter(adapter);
    }
}

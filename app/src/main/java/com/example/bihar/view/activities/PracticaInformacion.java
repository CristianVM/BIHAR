package com.example.bihar.view.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bihar.R;
import com.example.bihar.controller.GestorPracticas;
import com.example.bihar.model.Practica;

public class PracticaInformacion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.practica_informacion);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String IDPractica = extras.getString("IDPractica");
            Practica practica = GestorPracticas.getGestorPracticas().getPractica(IDPractica);
            TextView infoPracticaEmpresa = findViewById(R.id.infoPracticaEmpresa);
            infoPracticaEmpresa.setText(practica.getNombreEmpresa());
            TextView textLocalizacion2 = findViewById(R.id.textLocalizacion2);
            textLocalizacion2.setText(practica.getLocalidad() + " (" + practica.getProvincia() + ")");
            TextView textDescripcion2 = findViewById(R.id.textDescripcion2);
            textDescripcion2.setText(practica.getTitulo());
            TextView textFechas2 = findViewById(R.id.textFechas2);
            textFechas2.setText(practica.getFechaInicio() + " - " + practica.getFechaFin());
            TextView textHoras2 = findViewById(R.id.textHoras2);
            textHoras2.setText(practica.getHorasTotales());
            TextView textSalario2 = findViewById(R.id.textSalario2);
            textSalario2.setText(practica.getSalarioTotal() + "€");
        }
    }

    // ON DESTROY ELIMINAR IMAGEN DEL TELEFONO
}
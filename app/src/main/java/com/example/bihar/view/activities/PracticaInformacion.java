package com.example.bihar.view.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.GestorPracticas;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Practica;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.util.Locale;

public class PracticaInformacion extends AppCompatActivity {

    private String IDPractica;

    private String idiomaEstablecido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        idiomaEstablecido = prefs.getString("idioma", "es");
        if (idiomaEstablecido.equals("es")) {
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        } else if (idiomaEstablecido.equals("eu")) {
            Locale locale = new Locale("eu");
            cambiarIdiomaOnCreate(locale);
        }

        super.setContentView(R.layout.practica_informacion);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            IDPractica = extras.getString("IDPractica");
            obtenerImagenEmpresa();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaNuevo = sharedPreferences.getString("idioma","es");

        if(!idiomaNuevo.equals(idiomaEstablecido)){
            idiomaEstablecido = idiomaNuevo;
            if(idiomaEstablecido.equals("es")) {
                Locale locale = new Locale("es");
                cambiarIdiomaOnResume(locale);
            } else if(idiomaEstablecido.equals("eu")) {
                Locale locale = new Locale("eu");
                cambiarIdiomaOnResume(locale);
            }
        }
    }

    private void comenzarCarga() {
        Button buttonApuntarsePractica = findViewById(R.id.buttonApuntarsePractica);
        buttonApuntarsePractica.setClickable(false);

        ProgressBar infoPracticaProgressBar = findViewById(R.id.infoPracticaProgressBar);
        infoPracticaProgressBar.setVisibility(View.VISIBLE);
    }

    private void terminarCarga() {
        Button buttonApuntarsePractica = findViewById(R.id.buttonApuntarsePractica);
        buttonApuntarsePractica.setClickable(true);

        ProgressBar infoPracticaProgressBar = findViewById(R.id.infoPracticaProgressBar);
        infoPracticaProgressBar.setVisibility(View.INVISIBLE);
    }

    private void obtenerImagenEmpresa() {
        comenzarCarga();

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerImagenEmpresa");
        parametrosJSON.put("idPractica", IDPractica);

        Data datos = new Data.Builder()
                .putString("datos", parametrosJSON.toJSONString())
                .build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            try {
                                ImageView imageView = findViewById(R.id.infoPracticaLogoEmpresa);
                                File file = new File(getApplicationContext().getFilesDir(), IDPractica + ".png");
                                if (file.exists()) {
                                    imageView.setImageURI(Uri.fromFile(file));
                                } else {
                                    imageView.setImageResource(R.drawable.ic_work_black_24dp);
                                }
                                // Si salta algun error
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            } finally {
                                terminarCarga();
                                mostrarDatos();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void mostrarDatos() {
        Practica practica = GestorPracticas.getGestorPracticas().getPractica(IDPractica);
        TextView textLocalizacion2 = findViewById(R.id.textLocalizacion2);
        if(idiomaEstablecido.equals("es")) {
            textLocalizacion2.setText(practica.getLocalidad_es() + " (" + practica.getProvincia_es() + ")");
        } else {
            textLocalizacion2.setText(practica.getLocalidad_eu() + " (" + practica.getProvincia_eu() + ")");
        }
        TextView textDescripcion2 = findViewById(R.id.textDescripcion2);
        textDescripcion2.setText(practica.getTitulo());
        TextView textTareas2 = findViewById(R.id.textTareas2);
        textTareas2.setText(practica.getTareas());
        TextView textFechas2 = findViewById(R.id.textFechas2);
        textFechas2.setText(practica.getFechaInicio() + " - " + practica.getFechaFin());
        TextView textHoras2 = findViewById(R.id.textHoras2);
        textHoras2.setText(practica.getHorasTotales());
        TextView textSalario2 = findViewById(R.id.textSalario2);
        textSalario2.setText(practica.getSalarioTotal() + "€");

        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollViewInfoPractica);
        nestedScrollView.setVisibility(View.VISIBLE);
    }

    public void avisoInscripcion(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.practica_inscripcion_titulo);
        builder.setMessage(R.string.practica_inscripcion_mensaje);

        builder.setPositiveButton(R.string.Si, (dialog, which) -> inscripcionPractica());

        builder.setNegativeButton(R.string.No, (dialog, which) ->
                Toast.makeText(getApplicationContext(), R.string.practica_inscripcion_cancelada, Toast.LENGTH_LONG).show()
        );

        builder.show();
    }

    private void inscripcionPractica() {

        comenzarCarga();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "inscripcionPractica");
        parametrosJSON.put("idPractica", IDPractica);
        parametrosJSON.put("idAlumno", prefs.getString("idUsuario", ""));

        Data datos = new Data.Builder()
                .putString("datos", parametrosJSON.toJSONString())
                .build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            try {
                                String rdo = workInfo.getOutputData().getString("result");
                                JSONParser parser = new JSONParser();
                                JSONObject json = (JSONObject) parser.parse(rdo);
                                boolean exito = (boolean) json.get("exito");
                                // Si no ha habido errores
                                if (exito) {
                                    Toast.makeText(getApplicationContext(), R.string.practica_inscripcion_realizada, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                }
                            // Si salta algun error
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            } finally {
                                terminarCarga();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    /** Cambia el idioma de la aplicación al reanudarse la actividad. Se destruye la actividad y se
     *  vuelve a iniciar
     *  @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnResume(Locale locale) {
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        recreate();
    }

    /** Cambia el idioma de la aplicación al crearse la actividad
     *  @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnCreate(Locale locale){
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MY-APP", "DESTROY");
        File file = new File(getApplicationContext().getFilesDir(), IDPractica + ".png");
        file.delete();
    }
}
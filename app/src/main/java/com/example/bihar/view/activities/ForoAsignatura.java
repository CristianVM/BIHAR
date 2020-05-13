package com.example.bihar.view.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad encargada de mostrar los mensajes del foro para una asignatura en concreto
 */
public class ForoAsignatura extends AppCompatActivity {

    /**
     * Interfaz grafica de esta actividad basada en el codigo extraido del sitio web Scaledrone
     *
     * Fuente: https://www.scaledrone.com/blog/android-chat-tutorial/
     *
     * Modificado para simplicar y adaptar las necesidades a nuestra aplicacion
     */

    private AdapterForo adapterForo;
    private ArrayList<String> nombresProfesores = new ArrayList<>();
    private ArrayList<String> mensajesProfesores = new ArrayList<>();
    private ArrayList<String> fechasMensajes = new ArrayList<>();

    private String idAsignatura = "";
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

        super.setContentView(R.layout.lista_mensajes_foro);

        ToolBar toolbarForoAsignatura = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.toolbarForoAsignatura);
        toolbarForoAsignatura.cambiarTituloToolbar(getResources().getString(R.string.foro_titulo));

        RecyclerView listaMensajesForo = findViewById(R.id.listaMensajesForo);

        adapterForo = new AdapterForo(nombresProfesores, mensajesProfesores, fechasMensajes);
        listaMensajesForo.setAdapter(adapterForo);

        LinearLayoutManager elLayoutLineal = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true);
        listaMensajesForo.setLayoutManager(elLayoutLineal);

        listaMensajesForo.addItemDecoration(new GridSpacingItemDecoration(1, 20, true));

        // Si es alumno se le oculta la posibilidad de mandar un mensaje
        if (prefs.getBoolean("esAlumno", true)) {
            LinearLayout linearLayoutMandarMensaje = findViewById(R.id.linearLayoutMandarMensaje);
            linearLayoutMandarMensaje.setVisibility(View.GONE);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idAsignatura = extras.getString("idAsignatura");
            obtenerMensajesForoAsignatura();
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

    private void obtenerMensajesForoAsignatura() {
        comenzarCarga();

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerMensajesForoAsignatura");
        parametrosJSON.put("idAsignatura", idAsignatura);

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
                                JSONArray jsonArray = (JSONArray) parser.parse(rdo);
                                if (jsonArray.size() == 0) {
                                    TextView avisoForoVacio = findViewById(R.id.avisoForoVacio);
                                    avisoForoVacio.setVisibility(View.VISIBLE);
                                } else {
                                    for (int i = 0; i < jsonArray.size(); i++) {
                                        JSONObject json = (JSONObject) jsonArray.get(i);
                                        nombresProfesores.add((String) json.get("nombreProfesor"));
                                        mensajesProfesores.add((String) json.get("mensaje"));
                                        fechasMensajes.add((String) json.get("fecha"));
                                    }

                                    adapterForo.notifyDataSetChanged();
                                }
                            // Si salta algun error
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            } finally {
                                terminarCarga();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    public void mandarMensaje(View v) {
        EditText mensajeAMandar = findViewById(R.id.mensajeAMandar);
        String mensaje = mensajeAMandar.getText().toString();

        if(mensaje.trim().length() > 0) {
            ImageButton botonMandarMensajeForo = findViewById(R.id.botonMandarMensajeForo);
            botonMandarMensajeForo.setClickable(false);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            Date fecha = Calendar.getInstance().getTime();
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaFormateada = formato.format(fecha);

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("accion", "mandarMensajeForo");
            parametrosJSON.put("idAsignatura", idAsignatura);
            parametrosJSON.put("idPersona", prefs.getString("idUsuario", ""));
            parametrosJSON.put("mensaje", mensaje);
            parametrosJSON.put("fecha", fechaFormateada);

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
                                        nombresProfesores.add(0, prefs.getString("nombreUsuario", ""));
                                        mensajesProfesores.add(0, mensaje);
                                        fechasMensajes.add(0, fechaFormateada);

                                        adapterForo.notifyDataSetChanged();

                                        TextView avisoForoVacio = findViewById(R.id.avisoForoVacio);
                                        avisoForoVacio.setVisibility(View.INVISIBLE);

                                        String nombreProfesor = prefs.getString("nombreUsuario", "").split(" ")[0];
                                        String apellidoProfesor = prefs.getString("nombreUsuario", "").split(" ")[1];

                                        String mensajeNotificacion = getString(R.string.foro_mensaje_enviado, nombreProfesor + " " + apellidoProfesor);
                                        mandarNotificacion(idAsignatura, mensajeNotificacion, getString(R.string.foro_titulo));

                                        mensajeAMandar.setText("");
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                                    }
                                // Si salta algun error
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                } finally {
                                    ImageButton botonMandarMensajeForo = findViewById(R.id.botonMandarMensajeForo);
                                    botonMandarMensajeForo.setClickable(true);
                                }
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(otwr);
        } else {
            Toast.makeText(this, getString(R.string.mensaje_foro_vacio), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mandar una notificacion mediante Firebase a todos los alumnos de esta asignatura cuando el
     * profesor mande un mensaje
     *
     * @param idAsignatura: el id de la asignatura
     * @param mensajeNotificacion: el mensaje de la notificacion
     * @param tituloNotificacion: el titulo de la notificacion
     */
    private void mandarNotificacion(String idAsignatura, String mensajeNotificacion, String tituloNotificacion) {
        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "notificarForo");
        parametrosJSON.put("idAsignatura", idAsignatura);
        parametrosJSON.put("msg", mensajeNotificacion);
        parametrosJSON.put("titulo", tituloNotificacion);

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

        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void comenzarCarga() {
        ProgressBar progressBarForoAsignatura = findViewById(R.id.progressBarForoAsignatura);
        progressBarForoAsignatura.setVisibility(View.VISIBLE);

        ImageButton botonMandarMensajeForo = findViewById(R.id.botonMandarMensajeForo);
        botonMandarMensajeForo.setClickable(false);
    }

    private void terminarCarga() {
        ProgressBar progressBarForoAsignatura = findViewById(R.id.progressBarForoAsignatura);
        progressBarForoAsignatura.setVisibility(View.INVISIBLE);

        ImageButton botonMandarMensajeForo = findViewById(R.id.botonMandarMensajeForo);
        botonMandarMensajeForo.setClickable(true);
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
}

class ViewHolderForo extends RecyclerView.ViewHolder {

    public TextView nombreProfesorForo;
    public TextView mensajeProfesorForo;
    public TextView fechaMensajeForo;

    public ViewHolderForo(View itemView) {
        super(itemView);
        nombreProfesorForo = itemView.findViewById(R.id.nombreProfesorForo);
        mensajeProfesorForo = itemView.findViewById(R.id.mensajeProfesorForo);
        fechaMensajeForo = itemView.findViewById(R.id.fechaMensajeForo);
    }
}

class AdapterForo extends RecyclerView.Adapter<ViewHolderForo> {

    private ArrayList<String> losNombres;
    private ArrayList<String> losMensajes;
    private ArrayList<String> lasFechas;

    public AdapterForo(ArrayList<String> nombres, ArrayList<String> mensajes, ArrayList<String> fechas) {
        losNombres = nombres;
        losMensajes = mensajes;
        lasFechas = fechas;
    }

    public ViewHolderForo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutMensaje = LayoutInflater.from(parent.getContext()).inflate(R.layout.mensaje_foro, null);
        ViewHolderForo viewHolderForo = new ViewHolderForo(elLayoutMensaje);
        return viewHolderForo;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderForo holder, int position) {
        holder.nombreProfesorForo.setText(losNombres.get(position));
        holder.mensajeProfesorForo.setText(losMensajes.get(position));
        holder.fechaMensajeForo.setText(lasFechas.get(position));
    }

    @Override
    public int getItemCount() {
        return losMensajes.size();
    }
}
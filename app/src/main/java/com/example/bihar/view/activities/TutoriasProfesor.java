package com.example.bihar.view.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.GestorReservas;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Actividad en la que un Profesor podrá ver que alumnos le han solicitado asistir a una tutoría
 * El profesor podrá aceptar o rechazar esa petición.
 */
public class TutoriasProfesor extends AppCompatActivity {

    private ListView solicitadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutorias_profesor);

        solicitadas = findViewById(R.id.tutoriasProfesorListView);

        ToolBar toolbarTutoriasProfesor = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.toolbarTutoriasProfesor);
        toolbarTutoriasProfesor.cambiarTituloToolbar(getResources().getString(R.string.tutorias));

        cargarDatos();
    }

    /**
     * Se cargan los datos de la BD y se guardan en el Gestor
     */
    public void cargarDatos() {
        Map<String, String> map = new HashMap<>();
        map.put("accion", "obtenerDatosReservas");
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        JSONObject json = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos", json.toString());

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(data.build())
                .build();


        WorkManager.getInstance(this).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if (status != null && status.getState().isFinished()) {

                        if (status.getState() == WorkInfo.State.FAILED) {
                            finish();
                            return;
                        }

                        String resultado = status.getOutputData().getString("result");
                        GestorReservas.getGestorReservas().borrarReservas();
                        JSONParser parser = new JSONParser();
                        try {
                            JSONArray array = (JSONArray) parser.parse(resultado);
                            for (int i = 0; i < array.size(); i++) {
                                JSONObject obj = (JSONObject) array.get(i);
                                int idTutorias = Integer.parseInt((String) obj.get("idTutoria"));
                                String idPersona = (String) obj.get("idPersona");
                                String fecha = (String) obj.get("fecha");
                                int estado = Integer.parseInt((String) obj.get("estado"));
                                String msg = (String) obj.get("msg");
                                String nombreCompleto = (String) obj.get("nombreCompleto");

                                GestorReservas.getGestorReservas().anadirReserva(idTutorias, idPersona, fecha, estado, msg, nombreCompleto);
                            }

                            solicitadas.setAdapter(new MiListAdapter(this));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);
    }
}

class MiListAdapter extends BaseAdapter {

    private ArrayList<Integer> indices;
    private Activity activity;
    private int estadoActual;

    public MiListAdapter(Activity pActivity) {
        activity = pActivity;
        indices = new ArrayList<>();
        indices.addAll(GestorReservas.getGestorReservas().getIndices(0));
        indices.addAll(GestorReservas.getGestorReservas().getIndices(1));
        indices.addAll(GestorReservas.getGestorReservas().getIndices(2));
        estadoActual = -1;
    }

    @Override
    public int getCount() {
        return indices.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorias_profesor_listitem, null);

        int estado = GestorReservas.getGestorReservas().getEstado(indices.get(position));
        if (estadoActual != estado) {
            if (estadoActual != -1) {
                convertView.setPadding(0, 50, 0, 0);
            }
            estadoActual = estado;
        } else {
            convertView.setPadding(0, 0, 0, 0);
        }

        TextView nombreC = convertView.findViewById(R.id.reservaNombreCompleto);
        nombreC.setText(GestorReservas.getGestorReservas().getNombreCompleto(indices.get(position)));

        TextView txtestado = convertView.findViewById(R.id.reservaEstado);
        LinearLayout linearLayout = convertView.findViewById(R.id.reservaBotonesLayout);
        switch (estado) {
            case 0:
                txtestado.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);

                ImageView accept = convertView.findViewById(R.id.reservaAccept);
                ImageView deny = convertView.findViewById(R.id.reservaDeny);

                accept.setOnClickListener(v -> cambiarEstado(indices.get(position), 1));
                deny.setOnClickListener(v -> cambiarEstado(indices.get(position), 2));
                break;
            case 1:
                txtestado.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
                txtestado.setText(convertView.getContext().getString(R.string.aceptada));
                txtestado.setTextColor(Color.GREEN);
                break;
            case 2:
                txtestado.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
                txtestado.setText(convertView.getContext().getString(R.string.rechazada));
                txtestado.setTextColor(Color.RED);
                break;
        }

        TextView reservaFecha = convertView.findViewById(R.id.reservaFecha);
        String fecha = GestorReservas.getGestorReservas().getFecha(indices.get(position));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Date d = Date.valueOf(fecha);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(convertView.getContext());
            String idioma = prefs.getString("idioma", "es");

            Locale locale = new Locale(idioma, "es");
            SimpleDateFormat simpleDateformat = new SimpleDateFormat(convertView.getContext().getString(R.string.formatoFechaDia), locale);
            String strFecha = simpleDateformat.format(d);
            fecha = strFecha.substring(0, 1).toUpperCase() + strFecha.substring(1);
        }

        reservaFecha.setText(fecha);

        return convertView;
    }

    private void cambiarEstado(int position, int estado) {

        Map<String, String> map = new HashMap<>();
        map.put("accion", "cambiarEstadoReserva");
        map.put("idPersona", GestorReservas.getGestorReservas().getIdPersona(position));
        map.put("idTutoria", String.valueOf(GestorReservas.getGestorReservas().getIdTutoria(position)));
        map.put("estado", String.valueOf(estado));
        map.put("titulo", activity.getString(R.string.tutorias));

        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity);
        String nombre = sharedPreferences.getString("nombreUsuario", "");

        String aceptado_rechazado = estado == 1 ? activity.getString(R.string.aceptado) : activity.getString(R.string.rechazado);
        String msg = activity.getString(R.string.notificacionEstado, nombre, aceptado_rechazado);
        map.put("msg", msg);
        JSONObject json = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos", json.toString());

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(data.build())
                .build();


        WorkManager.getInstance(activity).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                (LifecycleOwner) activity, status -> {
                    if (status != null && status.getState().isFinished()) {

                        if (status.getState() == WorkInfo.State.FAILED) {
                            activity.finish();
                            return;
                        }

                        GestorReservas.getGestorReservas().setEstado(position, estado);
                        indices = new ArrayList<>();
                        indices.addAll(GestorReservas.getGestorReservas().getIndices(0));
                        indices.addAll(GestorReservas.getGestorReservas().getIndices(1));
                        indices.addAll(GestorReservas.getGestorReservas().getIndices(2));
                        notifyDataSetChanged();
                    }
                }
        );

        WorkManager.getInstance(activity).enqueue(trabajo);
    }
}

package com.example.bihar.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.view.activities.MapsUniversidad;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdapterListaUniversidades extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private String[] universidades;
    private String[] disponibilidades;
    private boolean[] estanDisponibles;
    private String[] idLibros;
    private String idPersona;
    private LifecycleOwner lifecycleOwner;
    private String[] latitudes;
    private String[] longitudes;
    private Activity activity;

    public AdapterListaUniversidades(Context context, String[] universidades, String[] disponibilidades,
                                     boolean[] estanDisponibles, String[] idLibros, String idPersona,
                                     LifecycleOwner lifecycleOwner, String[] latitudes, String[] longitudes,
                                     Activity activity) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.universidades = universidades;
        this.disponibilidades = disponibilidades;
        this.estanDisponibles = estanDisponibles;
        this.idLibros = idLibros;
        this.idPersona = idPersona;
        this.lifecycleOwner = lifecycleOwner;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return universidades.length;
    }

    @Override
    public Object getItem(int i) {
        return universidades[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.lista_universidades_libro, null);

        TextView txtUni = (TextView) view.findViewById(R.id.lista_libroInformacion_universidad);
        TextView txtDisponible = (TextView) view.findViewById(R.id.lista_libroInformacion_disponibilidad);
        Button btnLocalizacion = (Button) view.findViewById(R.id.lista_libroInformacion_btnLocalizacion);
        Button btnReserva = (Button) view.findViewById(R.id.lista_libroInformacion_btnReservar);
        ImageView imageView = (ImageView) view.findViewById(R.id.lista_libroInformacion_imgDisponible);

        txtUni.setText(universidades[i]);

        if (estanDisponibles[i]) {
            imageView.setImageResource(R.drawable.ic_libro_disponible);
            txtDisponible.setTextColor(context.getResources().getColor(R.color.verdeDisponible));
        } else {
            imageView.setImageResource(R.drawable.ic_libro_nodisponible);
            txtDisponible.setTextColor(Color.RED);
            btnReserva.setEnabled(false);
        }
        txtDisponible.setText(disponibilidades[i]);

        btnLocalizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verLocalizacion(i);
            }
        });

        btnReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizarReserva(i, imageView, txtDisponible, btnReserva);
            }
        });

        return view;
    }

    private void realizarReserva(int i, ImageView imageView, TextView txtDisponible, Button btnReserva) {
        Map<String, String> map = new HashMap<>();
        map.put("accion", "reservarLibro");
        map.put("idLibro", idLibros[i]);
        map.put("idPersona", idPersona);

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
        WorkManager.getInstance(context).enqueue(trabajo);

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                lifecycleOwner, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String resultado = status.getOutputData().getString("result");

                        if (!resultado.equals("NO")) {
                            imageView.setImageResource(R.drawable.ic_libro_nodisponible);
                            txtDisponible.setTextColor(Color.RED);
                            btnReserva.setEnabled(false);
                            disponibilidades[i] = context.getResources().getText(R.string.libroInformacion_libroNoDisponible).toString() + resultado;
                            txtDisponible.setText(disponibilidades[i]);

                            Toast.makeText(context.getApplicationContext(),
                                    context.getResources().getString(R.string.libroInformacion_reservaRealizada), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context.getApplicationContext(),
                                    context.getResources().getString(R.string.libroInformacion_reservaFallo), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void verLocalizacion(int pos){
        if(comprobarPermisos()){
            Intent i = new Intent(context, MapsUniversidad.class);
            i.putExtra("nombreUniversidad",universidades[pos]);
            i.putExtra("latitud",latitudes[pos]);
            i.putExtra("longitud",longitudes[pos]);

            context.startActivity(i);
        }

    }

    /**
     * Se comprueban los permisos. Si no se ha aceptado el permiso entonces se manda la petición de
     * que se acepten.
     * @return Si se acepta el permiso entonces devolverá true, sino false.
     */
    private boolean comprobarPermisos(){

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(activity.getApplicationContext(),
                        context.getResources().getString(R.string.libroInformacion_explPermiso),Toast.LENGTH_LONG).show();
            } else {
                /*Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                i.setData(uri);
                activity.startActivity(i);*/
            }
            ActivityCompat.requestPermissions(activity, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, 200);

        }else{
            return true;
        }
        return false;
    }
}

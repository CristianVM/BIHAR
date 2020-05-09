package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorProfesores;
import com.example.bihar.controller.GestorReservas;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Profesor;
import com.example.bihar.model.Usuario;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TutoriasProfesor extends AppCompatActivity {

    private ListView solicitadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestorUsuario.getGestorUsuario().setUsuario(new Usuario("bblanco","u@u.com"));

        setContentView(R.layout.activity_tutorias_profesor);

        solicitadas = findViewById(R.id.tutoriasProfesorListView);

        cargarDatos();
    }

    public void cargarDatos(){
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerDatosReservas");
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        JSONObject json = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos",json.toString());

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
                            for(int i = 0; i<array.size();i++){
                                JSONObject obj = (JSONObject) array.get(i);
                                int idTutorias = Integer.parseInt((String) obj.get("idTutoria"));
                                String idPersona = (String) obj.get("idPersona");
                                String fecha = (String) obj.get("fecha");
                                int estado = Integer.parseInt((String)obj.get("estado"));
                                String msg = (String) obj.get("msg");
                                String nombreCompleto = (String) obj.get("nombreCompleto");

                                GestorReservas.getGestorReservas().anadirReserva(idTutorias,idPersona,fecha,estado,msg,nombreCompleto);
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

class MiListAdapter extends BaseAdapter{

    private ArrayList<Integer> indices;
    private Activity activity;

    public MiListAdapter(Activity pActivity){
        activity = pActivity;
        indices = new ArrayList<>();
        indices.addAll(GestorReservas.getGestorReservas().getIndices(0));
        indices.addAll(GestorReservas.getGestorReservas().getIndices(1));
        indices.addAll(GestorReservas.getGestorReservas().getIndices(2));
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
        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorias_profesor_listitem,null);

        TextView nombreC = convertView.findViewById(R.id.reservaNombreCompleto);
        nombreC.setText(GestorReservas.getGestorReservas().getNombreCompleto(indices.get(position)));

        TextView estado = convertView.findViewById(R.id.reservaEstado);
        LinearLayout linearLayout = convertView.findViewById(R.id.reservaBotonesLayout);
        switch (GestorReservas.getGestorReservas().getEstado(indices.get(position))){
            case 0:
                estado.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                break;
            case 1:
                estado.setText("Aceptada");
                estado.setTextColor(Color.GREEN);
                break;
            case 2:
                estado.setText("Rechazada");
                estado.setTextColor(Color.RED);
                break;
        }

        return convertView;
    }
}

package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class InicioSesion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.contains("nombre")) {
            Stringasig01 = prefs.getString("nombre",null);

            setContentView(R.layout.inicio_sesion_usuario);
        } else {
            */setContentView(R.layout.inicio_sesion_general);
        //}

        //ProgressBar login1ProgressBar = findViewById(R.id.login1ProgressBar);
        //login1ProgressBar.setVisibility(View.INVISIBLE);
    }

    public void iniciarSesion(View v) {
        EditText login1EditUsuario = findViewById(R.id.login1EditUsuario);
        EditText login1EditContraseña = findViewById(R.id.login1EditContraseña);

        String usuario = login1EditUsuario.getText().toString();
        String password = login1EditContraseña.getText().toString();

        if(usuario.isEmpty()) {
            Toast.makeText(this, R.string.usuario_vacio, Toast.LENGTH_LONG).show();
        } else if(password.isEmpty()) {
            Toast.makeText(this, R.string.password_vacia, Toast.LENGTH_LONG).show();
        } else {
            Button login1BotonEntrar = findViewById(R.id.login1BotonEntrar);
            login1BotonEntrar.setClickable(false);

            ProgressBar login1ProgressBar = findViewById(R.id.login1ProgressBar);
            login1ProgressBar.setVisibility(View.VISIBLE);

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("accion", "iniciarSesion");
            parametrosJSON.put("idPersona", usuario);
            parametrosJSON.put("password", password);

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
                                        boolean existe = (boolean) json.get("existe");
                                        // Si el usuario existe y tiene esa password
                                        if (existe) {
                                            // Cargamos sus datos en SharedPreferences
                                            String idPersona = (String) json.get("idPersona");
                                            String nombre = (String) json.get("nombre");

                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("idPersona", usuario);
                                            editor.putString("nombre", nombre);

                                            // Si ha marcado recordar contraseña
                                            Switch login1SwitchRecordar = findViewById(R.id.login1SwitchRecordar);
                                            if(login1SwitchRecordar.isChecked()) {
                                                editor.putString("password", password);
                                                editor.apply();
                                            }

                                            Intent i = new Intent(getApplicationContext(), MenuPrincipal.class);
                                            startActivity(i);

                                            // Solicitamos la imagen asociada de manera individual
                                            //obtenerImagen(email);

                                            // Si alguno de los datos no es correcto
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.login_datos_incorrectos, Toast.LENGTH_LONG).show();
                                        }
                                        // Si ha habido algun error
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                    }
                                    // Si salta algun error
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                } finally {
                                    ProgressBar login1ProgressBar = findViewById(R.id.login1ProgressBar);
                                    login1ProgressBar.setVisibility(View.INVISIBLE);

                                    Button login1BotonEntrar = findViewById(R.id.login1BotonEntrar);
                                    login1BotonEntrar.setClickable(true);
                                }
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(otwr);
        }
    }
}
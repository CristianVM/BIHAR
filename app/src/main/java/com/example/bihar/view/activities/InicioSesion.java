package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
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
import com.google.android.material.textfield.TextInputLayout;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;

public class InicioSesion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.contains("nombreUsuario")) {
            setContentView(R.layout.inicio_sesion_usuario);
            ImageView login2ImageUsuario = findViewById(R.id.login2ImageUsuario);

            try {
                File file = new File(this.getFilesDir(), "perfil.png");
                if (file.exists()) {
                    login2ImageUsuario.setImageURI(Uri.fromFile(file));
                } else {
                    login2ImageUsuario.setImageResource(R.drawable.defecto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String nombreUsuario = prefs.getString("nombreUsuario",null);
            TextView textViewNombreUsuario = findViewById(R.id.login2NombreUsuario);
            textViewNombreUsuario.setText(nombreUsuario);

            if(prefs.contains("password")) {
                String password = prefs.getString("password",null);
                EditText loginEditPassword = findViewById(R.id.loginEditPassword);
                loginEditPassword.setText(password);
                Switch loginSwitchRecordar = findViewById(R.id.loginSwitchRecordar);
                loginSwitchRecordar.setChecked(true);

                TextInputLayout loginInputPassword = findViewById(R.id.loginInputPassword);
                loginInputPassword.setEndIconMode(TextInputLayout.END_ICON_NONE);
            }
        } else {
            setContentView(R.layout.inicio_sesion_general);
        }

        ProgressBar loginProgressBar = findViewById(R.id.loginProgressBar);
        loginProgressBar.setVisibility(View.INVISIBLE);
    }

    public void iniciarSesion(View v) {
        EditText login1EditUsuario = findViewById(R.id.login1EditUsuario);
        EditText loginEditPassword = findViewById(R.id.loginEditPassword);

        String usuario = "";
        String password = loginEditPassword.getText().toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(login1EditUsuario == null) {
            usuario = prefs.getString("idUsuario",null);
        } else {
            usuario = login1EditUsuario.getText().toString();
        }


        if(usuario.isEmpty()) {
            Toast.makeText(this, R.string.usuario_vacio, Toast.LENGTH_LONG).show();
        } else if(password.isEmpty()) {
            Toast.makeText(this, R.string.password_vacia, Toast.LENGTH_LONG).show();
        } else {
            comenzarCarga();

            JSONObject parametrosJSON = new JSONObject();
            parametrosJSON.put("accion", "iniciarSesion");
            parametrosJSON.put("idUsuario", usuario);
            parametrosJSON.put("password", password);
            parametrosJSON.put("token", prefs.getString("token", ""));

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
                                            String idUsuario = (String) json.get("idUsuario");
                                            String nombre = (String) json.get("nombre");
                                            boolean esAlumno = (boolean) json.get("esAlumno");

                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("idUsuario", idUsuario);
                                            editor.putString("nombreUsuario", nombre);
                                            editor.putBoolean("esAlumno", esAlumno);

                                            // Si ha marcado recordar contraseña
                                            Switch login1SwitchRecordar = findViewById(R.id.loginSwitchRecordar);
                                            if(login1SwitchRecordar.isChecked()) {
                                                editor.putString("password", password);
                                                editor.putString("fingerprintPassword", password);
                                            }

                                            editor.apply();

                                            if(login1EditUsuario != null) {
                                                obtenerImagenUsuario();
                                            } else {
                                                terminarCarga();
                                                Intent i = new Intent(getApplicationContext(), MenuPrincipal.class);
                                                startActivity(i);
                                            }

                                            // Si alguno de los datos no es correcto
                                        } else {
                                            terminarCarga();
                                            Toast.makeText(getApplicationContext(), R.string.login_datos_incorrectos, Toast.LENGTH_LONG).show();
                                        }
                                        // Si ha habido algun error
                                    } else {
                                        terminarCarga();
                                        Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                    }
                                    // Si salta algun error
                                } catch (Exception e) {
                                    terminarCarga();
                                    Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(otwr);
        }
    }

    public void cambiarUsuario(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        String token = prefs.getString("token", "");
        editor.clear();
        editor.putString("token", token);
        editor.apply();

        File file = new File(this.getFilesDir(), "perfil.png");
        file.delete();

        finish();
        startActivity(getIntent());
    }

    public void cambiarOpcionSwitch(View v) {
        Switch loginSwitchRecordar = findViewById(R.id.loginSwitchRecordar);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!loginSwitchRecordar.isChecked() && prefs.contains("password")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("password");
            editor.apply();
        }
    }

    public void passwordOlvidada(View v) {

    }

    public void accederHuella(View v) {

    }

    private void obtenerImagenUsuario() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idUsuario = prefs.getString("idUsuario", "");

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerImagen");
        parametrosJSON.put("idUsuario", idUsuario);

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
                                String resultado = workInfo.getOutputData().getString("result");
                                // Si se ha obtenido la imagen correctamente
                                if (resultado.equals("OK")) {
                                    Log.i("MY-APP", "IMAGEN USUARIO OBTENIDA");
                                } else {
                                    Log.i("MY-APP", "IMAGEN USUARIO NO OBTENIDA");
                                    File file = new File(getApplicationContext().getFilesDir(), "perfil.png");
                                    file.delete();
                                }
                                finish();
                                startActivity(getIntent());
                                Intent i = new Intent(getApplicationContext(), MenuPrincipal.class);
                                startActivity(i);
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

    private void comenzarCarga() {
        Button login1BotonEntrar = findViewById(R.id.loginBotonEntrar);
        login1BotonEntrar.setClickable(false);

        ProgressBar loginProgressBar = findViewById(R.id.loginProgressBar);
        loginProgressBar.setVisibility(View.VISIBLE);
    }

    private void terminarCarga() {
        ProgressBar loginProgressBar = findViewById(R.id.loginProgressBar);
        loginProgressBar.setVisibility(View.INVISIBLE);

        Button login1BotonEntrar = findViewById(R.id.loginBotonEntrar);
        login1BotonEntrar.setClickable(true);
    }
}
package com.example.bihar.view.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;

import org.json.simple.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AjustesPreferencias extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String CARPETA="misImagenes/";
    private final String RUTA_IMAGEN=CARPETA+"misFotos";

    private String path;
    private AlertDialog dialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.ajustes_preferencias);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean iniciado = prefs.getBoolean("iniciado",false);
        if(!iniciado){
            verPreferencias(false);
        }else{
            verPreferencias(true);
        }

        boolean notificacion = prefs.getBoolean("notificacion",true);
        SwitchPreference switchNotificacion = (SwitchPreference) findPreference("notificacion");
        switchNotificacion.setChecked(notificacion);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("idioma")){
            // si detecta cambios en la key idioma cambia el idioma.
            String idioma = sharedPreferences.getString("idioma", Locale.getDefault().getLanguage());
            cambiarIdioma(idioma);
        }else if(s.equals("gmail")) {
            EditTextPreference editTextPreference = findPreference("gmail");
            if (editTextPreference.getText().trim().length() > 0) {
                //SE COMPRUEBA SI ES CORRECTO EL EMAIL O NO
                // SE CREA LA EXPRESION REGULAR

                //https://es.stackoverflow.com/questions/46067/expresiones-regulares-para-correo-electr%C3%B3nico-en-java

                Pattern pattern = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@[a-z0-9-]+(.[a-z0-9-]+)*(.[a-z]{2,4})$");
                Matcher mather = pattern.matcher(editTextPreference.getText());
                if (mather.find()) {
                    //SI ES CORRECTO SE MODIFICA EN LA BASE DE DATOS
                    Map<String,String> map = new HashMap<>();
                    map.put("accion","modificarGmail");
                    map.put("gmail",editTextPreference.getText());
                    map.put("idPersona",sharedPreferences.getString("idUsuario",""));

                    JSONObject jsonWorker = new JSONObject(map);
                    Data.Builder data = new Data.Builder();
                    data.putString("datos",jsonWorker.toString());
                    workerAjustes(data);

                } else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.ajustes_emailError), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.ajustes_emailVacio), Toast.LENGTH_SHORT).show();
            }
        }else if(s.equals("contrasena")){
            EditTextPreference editTextPreference = findPreference("contrasena");
            String contrasena = editTextPreference.getText();
            if(contrasena.trim().length()>0){
                Map<String,String> map = new HashMap<>();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        String key = preference.getKey();
        if(key.equals("portalWeb")){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ehu.eus/"+pref.getString("idioma","")+"/"));
            startActivity(i);
        }else if(key.equals("notificacion")){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean notificacion = prefs.getBoolean("notificacion",true);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notificacion",!notificacion);
            editor.apply();

        }else if (key.equals("fotoPerfil")){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getText(R.string.dialog_foto_eleccion));

            final CharSequence[] opciones =
                    {getResources().getText(R.string.dialog_foto_capturar), getResources().getText(R.string.dialog_foto_galeria), getResources().getText(R.string.dialog_foto_cancelar)};

            builder.setItems(opciones, (dialogInterface, i) -> {
                if (opciones[i].equals(getResources().getText(R.string.dialog_foto_capturar))) {
                    capturarFoto();
                } else if (opciones[i].equals(getResources().getText(R.string.dialog_foto_galeria))) {
                    galeria();
                } else if (opciones[i].equals(getResources().getText(R.string.dialog_foto_cancelar))) {
                    cancelar();
                }
            });

            dialog = builder.create();
            dialog.show();
        }
        return true;
    }

    /**
     * Cambia el idioma de la aplicación
     * @param idim: idioma nuevo
     */
    private void cambiarIdioma(String idim){
        Locale nuevaloc = new Locale(idim);
        Locale.setDefault(nuevaloc);
        Configuration config = new Configuration();
        config.locale = nuevaloc;
        getActivity().getBaseContext().getResources().updateConfiguration(config,getActivity().getBaseContext().getResources()
                .getDisplayMetrics());
        getActivity().finish();
        Intent intent = getActivity().getIntent();
        startActivity(intent);
    }

    private void workerAjustes(Data.Builder data){
        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(data.build())
                .build();
        WorkManager.getInstance(getContext()).enqueue(trabajo);
    }

    private void verPreferencias(boolean ver){
        PreferenceCategory user = findPreference("keyUsuario");
        user.setVisible(ver);
    }

    /**
     * Método a ejecutar cuando es elegido la función de elegir una foto de la galeria. Se realiza una
     * intención y te da la posibilidad de escoger una aplicación para coger una foto.
     */
    public void galeria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        getActivity().startActivityForResult(Intent.createChooser(intent, getResources().getText(R.string.dialog_foto_seleccionar)), 80);
    }

    /**
     * Método a ejecutar cuando es elegido la función de capturar una foto. Primero se comprueban los permisos.
     * Si tiene los permisos adecuados entonces se comprobará si existe la carpeta donde irán las imágenes, si
     * no existe la carpeta se crea. Después se manda una intención para que se haga una foto y se espera a que
     * se finalice la intención.
     */
    public void capturarFoto() {
        boolean permisos = validaPermisos();

        // SI LOS PERMISOS ESTAN ACTIVADOS SE SACA LA FOTO
        if(permisos) {
            File fileImagen = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGEN);
            if (!fileImagen.exists()) {
                boolean a = fileImagen.mkdirs();
            }
            String nombreImg = "";
            if (fileImagen.exists()) {
                nombreImg = (System.currentTimeMillis() / 100) + ".png";

                path = Environment.getExternalStorageDirectory() + File.separator + RUTA_IMAGEN + File.separator + nombreImg;
                File imagen = new File(path);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String authorities = getActivity().getApplicationContext().getPackageName() + ".provider";
                    Uri imageUri = FileProvider.getUriForFile(getActivity(), authorities, imagen);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                } else {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("path",path);
                editor.apply();
                getActivity().startActivityForResult(intent, 81);
            }
        }
    }

    public void cancelar() {
        dialog.dismiss();
    }

    /**
     * Se comprueban los permisos. Si no se ha aceptado el permiso entonces se manda la petición de
     * que se acepten.
     * @return Si se acepta el permiso entonces devolverá true, sino false.
     */
    private boolean validaPermisos(){

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE )){
                Toast.makeText(getActivity().getApplicationContext(),
                        getContext().getResources().getString(R.string.libroInformacion_explPermiso),Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 15);
        }else{
            return true;
        }
        return false;
    }
}

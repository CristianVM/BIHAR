package com.example.bihar.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        SwitchPreference switchNotificacion = findPreference("notificacion");
        switchNotificacion.setChecked(notificacion);

        if(iniciado) {
            Preference editTextPreference = findPreference("gmail");
            String gmail = GestorUsuario.getGestorUsuario().getUsuario().getGmail();
            if (gmail != null && !gmail.isEmpty()) {
                editTextPreference.setSummary(gmail);
            } else {
                editTextPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        inicioSesionGoogle();
                        return true;
                    }
                });
            }
        }

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
                    abrirCamara();
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
        Intent elIntentGal= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(elIntentGal, 80);
    }

    /**
     * Método a ejecutar cuando es elegido la función de capturar una foto. Primero se comprueban los permisos.
     * Si tiene los permisos adecuados entonces se comprobará si existe la carpeta donde irán las imágenes, si
     * no existe la carpeta se crea. Después se manda una intención para que se haga una foto y se espera a que
     * se finalice la intención.
     */
   /* public void capturarFoto() {
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
    }*/

    public void abrirCamara(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},1);

            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                return;
            }
        }
        Intent elIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (elIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(elIntent, 82);
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
    /*private boolean validaPermisos(){

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
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("gmail",account.getEmail());
                editor.apply();

                //No dejamos guardado la cuenta de Google en la aplicación, para que el usuario pueda cambiarlo cuando quiera.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                mGoogleSignInClient.revokeAccess();

                Preference editTextPreference = findPreference("gmail");
                editTextPreference.setSummary(prefs.getString("gmail",getString(R.string.ajustes_gmail_descripcion)));

                Map<String,String> map = new HashMap<>();
                map.put("accion","modificarGmail");
                map.put("gmail",prefs.getString("gmail",""));
                map.put("idPersona",prefs.getString("idUsuario",""));

                JSONObject jsonWorker = new JSONObject(map);
                Data.Builder dataBuilder = new Data.Builder();
                dataBuilder.putString("datos",jsonWorker.toString());
                workerAjustes(dataBuilder);

            } catch (ApiException e) {
                Log.w("Error", "signInResult:failed code=" + e.getStatusCode());

                Toast.makeText(getActivity(), getString(R.string.error_general), Toast.LENGTH_LONG).show();
            }
        }else if(resultCode == Activity.RESULT_OK && requestCode==80){
            //RESULTADO DE LA GALERÍA
            try{
                Uri miPath  = data.getData();
                insertImageBD(true,miPath.toString());
            }catch(Exception e){
                Toast.makeText(getActivity(),"ERROR",Toast.LENGTH_LONG).show();
            }
        }else if(resultCode == Activity.RESULT_OK && requestCode==82){
            SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());

            String nombrefichero = prefs.getString("idUsuario","");
            File imagenFich= new File(getActivity().getApplicationContext().getFilesDir(), nombrefichero+ ".jpg");

            try {
                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                OutputStream os = new FileOutputStream(imagenFich);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();

                insertImageBD(true,imagenFich.toURI().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void inicioSesionGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }


    /**
     * Se inserta la imagen en la BD remota dependiendo de si es desde la galeria o desde la cámara.
     */
    private void insertImageBD(boolean esGaleria,String path){

        // SE MANDA LA IMAGEN A LA BASE DE DATOS GRACIAS AL WORKER
        Map<String,String> map = new HashMap<>();
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        map.put("path",path);
        map.put("accion","insertarFotoPerfil");
        if(esGaleria){
            map.put("esGaleria","true");
        }else{
            map.put("esGaleria","false");
        }

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
        WorkManager.getInstance(getActivity()).enqueue(trabajo);

        WorkManager.getInstance(getActivity())
                .getWorkInfoByIdLiveData(trabajo.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("result");

                        if(resultado.equals("Ok")){
                            obtenerImagenUsuario();
                        }else{
                            Toast.makeText(getActivity(),"ERROR",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void obtenerImagenUsuario() {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
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

        WorkManager.getInstance(getActivity())
                .getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        try {
                            String resultado = workInfo.getOutputData().getString("result");
                            // Si se ha obtenido la imagen correctamente
                            if (resultado.equals("OK")) {
                                Log.i("MY-APP", "IMAGEN USUARIO OBTENIDA");
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("imagenCambiada",true);
                                editor.apply();
                                Toast.makeText(getActivity(), getString(R.string.avisoImagen),Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i("MY-APP", "IMAGEN USUARIO NO OBTENIDA");
                                File file = new File(getActivity().getFilesDir(), idUsuario + ".png");
                                file.delete();
                            }
                            // Si salta algun error
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), R.string.error_general, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });

        WorkManager.getInstance(getActivity()).enqueue(otwr);
    }
}

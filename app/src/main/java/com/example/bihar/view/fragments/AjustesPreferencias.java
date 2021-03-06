package com.example.bihar.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AjustesPreferencias extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AlertDialog dialog;
    private LifecycleOwner lifecycleOwner;

    GoogleAccountCredential credential;
    private ArrayList<Evento> eventos;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.ajustes_preferencias);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean iniciado = prefs.getBoolean("iniciado", false);
        if (!iniciado) {
            verPreferencias(false);
        } else {
            verPreferencias(true);
        }

        // SE COMPRUEBA SI ESTÁ ACTIVADO O NO LA NOTIFICACIÓN
        boolean notificacion = prefs.getBoolean("notificacion", true);
        SwitchPreference switchNotificacion = findPreference("notificacion");
        switchNotificacion.setChecked(notificacion);

        if (iniciado) {
            actualizarCampoEmail();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        //NO RECOGE LS CAMBIOS REALIZADOS
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // RECOGE LOS CAMBIOS REALIZADOS
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Al cambiar alguna de las preferencias, se ejecuta una función
     *
     * @param sharedPreferences: shared preferences
     * @param s:                 la key de la preferencias
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // SI SE MODIFICA LA PREFERENCIA DE LA KEY IDIOMA SE CAMBIA EN LAS PREFERENCIAS
        if (s.equals("idioma")) {
            // si detecta cambios en la key idioma cambia el idioma.
            String idioma = sharedPreferences.getString("idioma", Locale.getDefault().getLanguage());
            cambiarIdioma(idioma);
        }
    }

    /**
     * Al hacerle click en una preferencia, se activa su función.
     *
     * @param preference: la preferencia clickada
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        String key = preference.getKey();
        if (key.equals("portalWeb")) {
            // AL HACERLE CLICK AL PORTAL WEB TE DIRIGE A LA WEB DE LA UPV-EHU
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ehu.eus/" + pref.getString("idioma", "") + "/"));
            startActivity(i);
        } else if (key.equals("notificacion")) {
            //AL HACERLE CLICK A LA SWITCH DE LAS NOTIFICACIONES, SE MODIFICA EN LAS SHARED PREFERENCES
            // EL ESTADO DE LA NOTIFICACION Y TAMBIEN SE ACTUALIZA EN LA BASE DE DATOS REMOTA
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            SharedPreferences.Editor editor = prefs.edit();
            SwitchPreference switchNotificacion = findPreference("notificacion");

            Map<String, String> map = new HashMap<>();
            map.put("accion", "updateNotificacion");
            map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());

            if (switchNotificacion.isChecked()) {
                editor.putBoolean("notificacion", true);
                map.put("notificacion", "true");
            } else {
                editor.putBoolean("notificacion", false);
                map.put("notificacion", "false");
            }
            editor.apply();

            // SE PREPARA PARA MODIFICAR EN LA BASE DE DATOS
            Data.Builder data = new Data.Builder();
            data.putString("datos", new JSONObject(map).toString());

            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                    .setConstraints(restricciones)
                    .setInputData(data.build())
                    .build();
            WorkManager.getInstance(getActivity()).enqueue(trabajo);

        } else if (key.equals("fotoPerfil")) {
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
        } else if (key.equals("contrasena")) {
            // SI SE HACE CLICK EN LA PREFERENCIA DE CAMBIAR LA CONTRASEÑA

            // SE ABRE UN DIALOGO DONDE TIENES QUE ESCRIBIR LA CONTRASEÑA ACTUAL Y LA NUEVA
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View vista = inflater.inflate(R.layout.dialog_cambiocontrasenia, null);

            builder.setView(vista);

            TextView txtPersona = (TextView) vista.findViewById(R.id.dialog_cambioContrasena_persona);
            txtPersona.setText(GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
            Button btn = (Button) vista.findViewById(R.id.dialog_cambioContrasena_boton);
            AlertDialog dialog = builder.create();

            btn.setOnClickListener(view -> {

                //AL DARLE CLICK AL BOTON DE CAMBIAR LA CONTRASEÑA
                EditText antigua = (EditText) vista.findViewById(R.id.dialog_cambioContrasena_contrasenaAnterior);
                EditText nueva = (EditText) vista.findViewById(R.id.dialog_cambioContrasena_contrasenaNueva);
                String txtAntigua = antigua.getText().toString();
                String txtNueva = nueva.getText().toString();

                if (!txtAntigua.equals("") && !txtNueva.equals("")) {
                    //SE COMPRUEBA QUE NO ESTÁN VACÍOS Y SE MANDA LA NUEVA CONTRASEÑA A LA BASE DE DATOS
                    Map<String, String> map = new HashMap<>();
                    map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
                    map.put("antigua", txtAntigua);
                    map.put("nueva", txtNueva);
                    map.put("accion", "cambioContrasena");

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
                    WorkManager.getInstance(getActivity()).enqueue(trabajo);

                    WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                            lifecycleOwner, status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String resultado = status.getOutputData().getString("result");
                                    if (resultado.equals("Ok")) {
                                        // SI SE HA MODIFICADO LA CONTRASEÑA CORRECTAMENTE
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                                        if (prefs.contains("password")) {
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("password", txtNueva);
                                            editor.apply();
                                        }
                                        Toast.makeText(getContext(), getActivity().getResources().getString(R.string.ajustes_contrasenaCambiada), Toast.LENGTH_SHORT).show();
                                    } else {
                                        //NO SE HA MODIFICADO LA CONTRASEÑA
                                        Toast.makeText(getContext(), getActivity().getResources().getString(R.string.ajustes_contrasenaNoCambiada), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    dialog.dismiss();

                } else {
                    // ALGÚN CAMPO ESTÁ VACÍO
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.dialog_notaprofesor_vacio), Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
        return true;
    }

    /**
     * Cambia el idioma de la aplicación
     *
     * @param idim: idioma nuevo
     */
    private void cambiarIdioma(String idim) {
        Locale nuevaloc = new Locale(idim);
        Locale.setDefault(nuevaloc);
        Configuration config = new Configuration();
        config.locale = nuevaloc;
        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources()
                .getDisplayMetrics());
        getActivity().finish();
        Intent intent = getActivity().getIntent();
        startActivity(intent);
    }

    private void actualizarCampoEmail() {
        Preference editTextPreference = findPreference("gmail");
        String gmail = GestorUsuario.getGestorUsuario().getUsuario().getGmail();
        if (gmail != null && !gmail.isEmpty()) {
            editTextPreference.setSummary(gmail);
            editTextPreference.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getContext()).setMessage(getString(R.string.desvincularCuenta))
                        .setPositiveButton(R.string.Si, (dialog, which) -> {
                            guardarEmail(null);
                            desvincularCuentaGoogle();
                        }).setNegativeButton(R.string.No, null).show();
                return true;
            });
        } else {
            editTextPreference.setSummary(getString(R.string.ajustes_gmail_descripcion));
            editTextPreference.setOnPreferenceClickListener(preference -> {
                inicioSesionGoogle();
                return true;
            });
        }
    }

    private void workerAjustes(Data.Builder data) {
        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(data.build())
                .build();
        WorkManager.getInstance(getContext()).enqueue(trabajo);
    }

    private void verPreferencias(boolean ver) {
        PreferenceCategory user = findPreference("keyUsuario");
        user.setVisible(ver);

        SwitchPreference sw = findPreference("notificacion");
        sw.setVisible(ver);
    }

    /**
     * Método a ejecutar cuando es elegido la función de elegir una foto de la galeria. Se realiza una
     * intención y te da la posibilidad de escoger una aplicación para coger una foto.
     */
    public void galeria() {
        Intent elIntentGal = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(elIntentGal, 80);
    }

    /**
     * Método a ejecutar cuando es elegido la función de capturar una foto. Primero se comprueban los permisos.
     * Si tiene los permisos adecuados entonces se comprobará si existe la carpeta donde irán las imágenes, si
     * no existe la carpeta se crea. Después se manda una intención para que se haga una foto y se espera a que
     * se finalice la intención.
     */

    public void abrirCamara() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 82);
        } else {
            ejecutarCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 82: {
                // Si la petición se cancela, granResults estará vacío
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, abrir camara
                    ejecutarCamara();
                } else {
                    // Permiso denegado, mostrar aviso
                    // En caso de seleccionar no volver a preguntar de nuevo el usuario tendra
                    // que acceder manualmente a los ajustes de la aplicacion
                    Toast.makeText(getActivity(), getString(R.string.permiso_denegado), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void ejecutarCamara() {
        Intent elIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (elIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(elIntent, 82);
        }
    }

    public void cancelar() {
        dialog.dismiss();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("REQUESTCODE", "=" + requestCode);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(googleSignInAccount -> {
                guardarEmail(googleSignInAccount.getEmail());


                credential = GoogleAccountCredential.usingOAuth2(
                        getContext(), Collections.singletonList(CalendarScopes.CALENDAR))
                        .setBackOff(new ExponentialBackOff())
                        .setSelectedAccountName(googleSignInAccount.getAccount().name);


                recogerEventos();

            }).addOnFailureListener(e -> Toast.makeText(getActivity(), getString(R.string.error_general), Toast.LENGTH_SHORT).show());

        } else if (resultCode == Activity.RESULT_OK && requestCode == 80) {
            //RESULTADO DE LA GALERÍA
            try {
                Uri miPath = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), miPath);

                SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
                String nombrefichero = prefs.getString("idUsuario", "");
                File imagenFich = new File(getActivity().getApplicationContext().getFilesDir(), nombrefichero + ".png");
                OutputStream os = new FileOutputStream(imagenFich);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();

                insertImageBD(true, miPath.toString());
            } catch (Exception e) {
                Toast.makeText(getActivity(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 82) {
            SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
            String nombrefichero = prefs.getString("idUsuario", "");
            File imagenFich = new File(getActivity().getApplicationContext().getFilesDir(), nombrefichero + ".png");

            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                OutputStream os = new FileOutputStream(imagenFich);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();

                insertImageBD(true, imagenFich.toURI().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            new MiAsyncTask(this).execute(eventos.toArray(new Evento[0]));
        }
    }

    private void inicioSesionGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar.events"))
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }


    private void guardarEmail(String pEmail) {

        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("gmail", pEmail);
        editor.apply();

        GestorUsuario.getGestorUsuario().getUsuario().setGmail(pEmail);
        actualizarCampoEmail();

        Map<String, String> map = new HashMap<>();
        map.put("accion", "modificarGmail");
        map.put("gmail", prefs.getString("gmail", null));
        map.put("idPersona", prefs.getString("idUsuario", ""));

        JSONObject jsonWorker = new JSONObject(map);
        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("datos", jsonWorker.toString());
        workerAjustes(dataBuilder);
    }

    public void desvincularCuentaGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        mGoogleSignInClient.revokeAccess();
    }


    /**
     * Se inserta la imagen en la BD remota dependiendo de si es desde la galeria o desde la cámara.
     */
    private void insertImageBD(boolean esGaleria, String path) {

        // SE MANDA LA IMAGEN A LA BASE DE DATOS GRACIAS AL WORKER
        Map<String, String> map = new HashMap<>();
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        map.put("path", path);
        map.put("accion", "insertarFotoPerfil");
        if (esGaleria) {
            map.put("esGaleria", "true");
        } else {
            map.put("esGaleria", "false");
        }

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
        WorkManager.getInstance(getActivity()).enqueue(trabajo);

        WorkManager.getInstance(getActivity())
                .getWorkInfoByIdLiveData(trabajo.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("result");

                        if (resultado.equals("Ok")) {
                            SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("imagenCambiada", true);
                            editor.apply();
                            Toast.makeText(getActivity(), getString(R.string.avisoImagen), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Recoge el Lifecycle de la actividad Ajustes
     *
     * @param lifecycleOwner: lifecycleOwner ajustes
     */
    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public void recogerEventos() {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerEventosUsuario");
        parametrosJSON.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        parametrosJSON.put("idioma", prefs.getString("idioma", "es"));

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
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Toast.makeText(getContext(), getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String resultado = workInfo.getOutputData().getString("result");

                        eventos = new ArrayList<>();

                        try {
                            JSONArray array = (JSONArray) new JSONParser().parse(resultado);
                            for (int i = 0; i < array.size(); i++) {
                                JSONObject obj = (JSONObject) array.get(i);
                                boolean esTutoria = (boolean) obj.get("esTutoria");
                                String nombreEvento = (String) obj.get("nombreEvento");

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                                LocalDateTime fechaExamenInicio = LocalDateTime.parse((String) Objects.requireNonNull(obj.get("fechaExamenInicio")), formatter);
                                String strFechaFinal = (String) obj.get("fechaExamenFinal");
                                LocalDateTime fechaExamenFinal;
                                if (strFechaFinal == null) {
                                    fechaExamenFinal = null;
                                } else {
                                    fechaExamenFinal = LocalDateTime.parse(strFechaFinal, formatter);
                                }
                                String aulaExamen = (String) obj.get("aulaExamen");
                                String nombreCentro = (String) obj.get("nombreCentro");

                                eventos.add(new Evento(esTutoria, fechaExamenInicio, fechaExamenFinal, nombreEvento, nombreCentro, aulaExamen));
                            }

                            new MiAsyncTask(this).execute(eventos.toArray(new Evento[0]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                });

        WorkManager.getInstance(getActivity()).enqueue(otwr);


    }

    public void notificarCambio() {
        Toast.makeText(getContext(), getString(R.string.datosGuardados), Toast.LENGTH_SHORT).show();
    }


}

class MiAsyncTask extends AsyncTask<Evento, Void, Integer> {

    private AjustesPreferencias fragment;

    MiAsyncTask(AjustesPreferencias pFragment) {
        fragment = pFragment;
    }


    @Override
    protected Integer doInBackground(Evento... params) {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.calendar.Calendar service = new Calendar.Builder(
                transport, jsonFactory, fragment.credential)
                .setApplicationName("BIHAR")
                .build();

        try {
            for (Evento e : params) {
                crearEvento(e, service);
            }

            return 1;
        } catch (UserRecoverableAuthIOException e) {
            fragment.startActivityForResult(e.getIntent(), 101);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (integer == 1) {
            fragment.notificarCambio();
        }
    }

    private void crearEvento(Evento evento, Calendar service) throws IOException {


        if (existeEvento(service, evento)) {
            return;
        }

        String summary = (evento.esTutoria() ? fragment.getString(R.string.tutoria) : fragment.getString(R.string.examen)) +
                " " + evento.getNombreEvento();

        Log.i("Evento", summary);
        Event event = new Event()
                .setSummary(summary)
                .setLocation(evento.getCentro())
                .setDescription(fragment.getString(R.string.aula) + " " + evento.getClase());

        DateTime startDateTime = new DateTime(evento.getFechaExamenInicio());
        EventDateTime start = new EventDateTime()
                .setTimeZone("Europe/Madrid")
                .setDateTime(startDateTime);
        event.setStart(start);

        DateTime endDateTime = new DateTime(evento.getFechaExamenFinal());
        EventDateTime end = new EventDateTime()
                .setTimeZone("Europe/Madrid")
                .setDateTime(endDateTime);
        event.setEnd(end);


        String calendarId = "primary";

        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());


    }

    //Evitamos que se creen eventos duplicados
    private boolean existeEvento(Calendar service, Evento evento) throws IOException {
        Events events = service.events().list("primary")
                .setMaxResults(1)
                .setTimeZone("Europe/Madrid")
                .setTimeMin(DateTime.parseRfc3339(evento.getFechaExamenInicio()))
                .setTimeMax(DateTime.parseRfc3339(evento.getFechaExamenFinal()))
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();


        List<Event> items = events.getItems();


        return items.size() > 0;
    }
}


class Evento {
    private boolean esTutoria;
    private LocalDateTime fechaExamenInicio;
    private LocalDateTime fechaExamenFinal;
    private String nombreEvento;
    private String centro;
    private String clase;

    private DateTimeFormatter formatter;

    public Evento(boolean esTutoria, LocalDateTime fechaExamenInicio, LocalDateTime fechaExamenFinal, String nombreEvento, String centro, String clase) {
        this.esTutoria = esTutoria;

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        this.fechaExamenInicio = fechaExamenInicio.minusHours(2);
        this.fechaExamenFinal = fechaExamenFinal;
        if (this.fechaExamenFinal != null) {
            this.fechaExamenFinal = this.fechaExamenFinal.minusHours(2);
        }
        this.nombreEvento = nombreEvento;
        this.centro = centro;
        this.clase = clase;
    }

    public Evento(boolean esTutoria, LocalDateTime fechaExamenInicio, String nombreEvento, String centro, String clase) {
        this(esTutoria, fechaExamenInicio, null, nombreEvento, centro, clase);
    }

    boolean esTutoria() {
        return esTutoria;
    }

    String getFechaExamenInicio() {
        return fechaExamenInicio.format(formatter);
    }

    String getFechaExamenFinal() {
        if (fechaExamenFinal == null)
            fechaExamenFinal = fechaExamenInicio.plusHours(2);
        return fechaExamenFinal.format(formatter);
    }

    String getNombreEvento() {
        return nombreEvento;
    }

    String getCentro() {
        return centro;
    }

    String getClase() {
        if (clase == null)
            clase = "";
        return clase;
    }
}



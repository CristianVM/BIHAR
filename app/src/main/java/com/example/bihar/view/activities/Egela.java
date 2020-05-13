package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.bihar.R;
import com.example.bihar.view.fragments.ToolBar;

import java.util.Locale;

/**
 * Actividad que corresponde al acceso a eGela por medio de la aplicación
 */
public class Egela extends AppCompatActivity {

    private static final String URL_EGELA = "https://egela.ehu.eus/";

    private WebView webView;

    private String idiomaEstablecido;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        idiomaEstablecido = prefs.getString("idioma","es");
        if(idiomaEstablecido.equals("es")){
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        }else if(idiomaEstablecido.equals("eu")){
            Locale locale = new Locale("eu");
            cambiarIdiomaOnCreate(locale);
        }
        setContentView(R.layout.activity_egela);

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        //Usaremos la query en la url para determinar el idioma de la página web
        webView.loadUrl(URL_EGELA + "?lang=" + idiomaEstablecido);

        ToolBar toolbarEgela = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.toolbarEgela);
        toolbarEgela.cambiarTituloToolbar(getResources().getString(R.string.egela));
    }

    @Override
    public void onBackPressed() {
        if(!webView.canGoBack()){
            finish();
        }else{
            webView.goBack();
        }
    }

    /**
     * Se comprueba el idioma que tenía la actividad con el de SharedPreferences: si es distinto se
     * cambia el idioma cerrando y volviendo a iniciar la actividad.
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String idiomaNuevo = sharedPreferences.getString("idioma","es");

        if(!idiomaNuevo.equals(idiomaEstablecido)){
            idiomaEstablecido = idiomaNuevo;
            if(idiomaEstablecido.equals("es")){
                Locale locale = new Locale("es");
                cambiarIdiomaOnResume(locale);
            }else if(idiomaEstablecido.equals("eu")){
                Locale locale = new Locale("eu");
                cambiarIdiomaOnResume(locale);
            }
        }
    }
    /**
     * Cambia el idioma de la aplicación al reanudarse la actividad. Se destruye la actividad y se
     * vuelve a iniciar
     * @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnResume(Locale locale){
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        recreate();
    }

    /**
     * Cambia el idioma de la aplicación al crearse la actividad
     * @param locale: el idioma almacenado en SharedPreferences
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

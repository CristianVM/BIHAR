package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.model.CardOpcion;
import com.example.bihar.model.ListaOpcionesMenu;

import java.util.Locale;

public class MenuPrincipal extends AppCompatActivity {

    private static final int TIEMPO_ANIMACION_MS = 1250;

    RecyclerView recyclerView;
    private String idiomaEstablecido;


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

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("iniciado",true);
        editor.apply();

        setContentView(R.layout.activity_menuprincipal);

        boolean esAlumno = prefs.getBoolean("esAlumno",false);
        CardOpcion[] opciones = null;
        if(esAlumno) {
            opciones = ListaOpcionesMenu.getListaOpcionesUsuario();
            Bundle bundle = getIntent().getExtras();
            if(bundle != null){
                boolean expediente = bundle.getBoolean("expediente");
                if(expediente) {
                    TextView notaMedia = findViewById(R.id.txtNotaMedia);
                    notaMedia.setText(String.valueOf(GestorUsuario.getGestorUsuario().getUsuario().getNotaMedia()));
                    opciones = ListaOpcionesMenu.getListaOpcionesExpediente();
                    animacionCreditos(GestorUsuario.getGestorUsuario().getUsuario().getNumCreditos());
                }
            }
        }
        else
            opciones = ListaOpcionesMenu.getListaOpcionesProfesor();

        recyclerView = findViewById(R.id.menuRecyclerView);

        ElAdaptadorRecycler elAdaptadorRecycler = new ElAdaptadorRecycler(opciones);
        recyclerView.setAdapter(elAdaptadorRecycler);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        int spanCount = 2; // 2 columns
        int spacing = 50; // 50px
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
    }

    public void animacionCreditos(float numCreditos){
        LinearLayout linearLayout = findViewById(R.id.menuPrincipal_extraLayout);
        linearLayout.setVisibility(View.VISIBLE);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(240);
        TextView txtProgressBar = findViewById(R.id.progressBarTxt);
        final int[] i = {0};
        ProgressBarAnimation anim = new ProgressBarAnimation(txtProgressBar, progressBar, 0, numCreditos);
        anim.setDuration(TIEMPO_ANIMACION_MS);
        progressBar.startAnimation(anim);

    }


    class ElAdaptadorRecycler extends RecyclerView.Adapter<MiViewHolder>{

        CardOpcion[] opciones;

        public ElAdaptadorRecycler(CardOpcion[] pOpciones){
            opciones = pOpciones;
        }

        @NonNull
        @Override
        public MiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View elLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_cardview, null);
            MiViewHolder evh = new MiViewHolder(elLayout);
            return evh;
        }

        @Override
        public void onBindViewHolder(@NonNull MiViewHolder holder, int position) {
            holder.imgButton.setImageResource(opciones[position].getImagen());
            holder.imgButton.setOnClickListener(v -> {
                Intent intent = null;
                switch (opciones[position].getTag()){
                    case "matricula":{
                        intent = new Intent(MenuPrincipal.this, Matricula.class);
                        break;
                    }
                    case "expediente":{
                        intent = new Intent(MenuPrincipal.this, MenuPrincipal.class);
                        intent.putExtra("expediente",true);
                        break;
                    }
                    case "horarios":{
                        Log.i("HORARIOS","AAAA");
                        intent = new Intent(MenuPrincipal.this, Horarios.class);
                        break;
                    }
                    case "tutorias":{
                        intent = new Intent(MenuPrincipal.this, Tutorias.class);
                        break;
                    }
                    case "biblioteca":{
                        intent = new Intent(MenuPrincipal.this, Biblioteca.class);
                        break;
                    }
                    case "practicas":{
                        intent = new Intent(MenuPrincipal.this, Practicas.class);
                        break;
                    }
                    case "egela":{
                        intent = new Intent(MenuPrincipal.this, Egela.class);
                        break;
                    }
                    case "foro":{
                        break;
                    }
                    case "asignaturas":{
                        intent = new Intent(MenuPrincipal.this, Asignaturas.class);
                        break;
                    }
                    case "creditos":{
                        break;
                    }
                    case "logros":{
                        Toast.makeText(MenuPrincipal.this, getString(R.string.noDisponible), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case "foroProfesor":{
                        break;
                    }
                    case "notasProfesor":{
                        break;
                    }
                    case "tutoriasProfesor":{
                        intent = new Intent(MenuPrincipal.this,TutoriasProfesor.class);
                        break;
                    }
                }

                if(intent != null)
                    startActivity(intent);

            });
        }

        @Override
        public int getItemCount() {
            return opciones.length;
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

class MiViewHolder extends RecyclerView.ViewHolder{

    public ImageView imgButton;

    public MiViewHolder(@NonNull View itemView) {
        super(itemView);
        imgButton = itemView.findViewById(R.id.btnOpcion);
    }
}

/**
 * Clase utilizada para el espaciado de elementos en el RecyclerView
 * Pregunta: https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
 * Autor: https://stackoverflow.com/users/1676363/ianhanniballake
 */
class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom
        } else {
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing; // item top
            }
        }
    }
}

/**
 * Clase para la animacion a la hora de mostrar el numero de créditos. Código adaptado para insertar un TextView
 * Pregunta: https://stackoverflow.com/questions/8035682/animate-progressbar-update-in-android
 * Autor: https://stackoverflow.com/users/102092/eli-konky
 */
class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private TextView textView;
    private float from;
    private float  to;

    public ProgressBarAnimation(TextView textView, ProgressBar progressBar, float from, float to) {
        super();
        this.textView = textView;
        this.progressBar = progressBar;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar.setProgress((int) value);
        String strProgreso = progressBar.getProgress() + "/" + 240;
        textView.setText(strProgreso);

    }

}
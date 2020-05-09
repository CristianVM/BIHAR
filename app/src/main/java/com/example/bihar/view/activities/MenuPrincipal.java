package com.example.bihar.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bihar.model.CardOpcion;
import com.example.bihar.model.ListaOpcionesMenu;
import com.example.bihar.R;

public class MenuPrincipal extends AppCompatActivity {

    private static final int TIEMPO_ANIMACION_MS = 1250;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menuprincipal);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean esAlumno = prefs.getBoolean("esAlumno",false);
        CardOpcion[] opciones = null;
        if(esAlumno) {
            opciones = ListaOpcionesMenu.getListaOpcionesUsuario();
            Bundle bundle = getIntent().getExtras();
            if(bundle != null){
                boolean expediente = bundle.getBoolean("expediente");
                if(expediente) {
                    opciones = ListaOpcionesMenu.getListaOpcionesExpediente();
                    animacionCreditos(156);
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

    public void animacionCreditos(int numCreditos){
        LinearLayout linearLayout = findViewById(R.id.menuPrincipal_extraLayout);
        linearLayout.setVisibility(View.VISIBLE);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(240);
        TextView txtProgressBar = findViewById(R.id.progressBarTxt);
        Handler h = new Handler();
        final int[] i = {0};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(i[0] < numCreditos) {

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(i[0]);
                            String s = i[0] + "/240";
                            txtProgressBar.setText(s);
                        }
                    });

                    try {
                        Thread.sleep(TIEMPO_ANIMACION_MS/numCreditos);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i[0]++;
                }
                progressBar.setProgress(numCreditos);
                String s = numCreditos +"/240";
                txtProgressBar.setText(s);
            }
        });
        thread.start();
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
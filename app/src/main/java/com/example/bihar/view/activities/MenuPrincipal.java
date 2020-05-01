package com.example.bihar.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bihar.model.CardOpcion;
import com.example.bihar.model.ListaOpcionesMenu;
import com.example.bihar.R;

public class MenuPrincipal extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menuprincipal);

        recyclerView = findViewById(R.id.menuRecyclerView);

        ElAdaptadorRecycler elAdaptadorRecycler = new ElAdaptadorRecycler(ListaOpcionesMenu.getListaOpcionesUsuario());
        recyclerView.setAdapter(elAdaptadorRecycler);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        int spanCount = 2; // 2 columns
        int spacing = 50; // 50px
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
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
                        //intent = new Intent(MenuPrincipal.this, Matricula.class);
                        Toast.makeText(MenuPrincipal.this, "No hecho aún", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case "horarios":{
                        //intent = new Intent(MenuPrincipal.this, Matricula.class);
                        Toast.makeText(MenuPrincipal.this, "No hecho aún", Toast.LENGTH_SHORT).show();
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
                        //intent = new Intent(MenuPrincipal.this, Matricula.class);
                        Toast.makeText(MenuPrincipal.this, "No hecho aún", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case "egela":{
                        //intent = new Intent(MenuPrincipal.this, Matricula.class);
                        Toast.makeText(MenuPrincipal.this, "No hecho aún", Toast.LENGTH_SHORT).show();
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

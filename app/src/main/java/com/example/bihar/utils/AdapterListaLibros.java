package com.example.bihar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bihar.R;

import java.util.ArrayList;

public class AdapterListaLibros extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Integer> imagenes;
    private ArrayList<String> autores;
    private ArrayList<String> titulos;
    private ArrayList<String> fechas;

    public AdapterListaLibros(Context context,ArrayList<Integer> imagenes, ArrayList<String> autores,
                              ArrayList<String> titulos, ArrayList<String> fechas) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imagenes = imagenes;
        this.autores = autores;
        this.titulos = titulos;
        this.fechas = fechas;
    }

    @Override
    public int getCount() {
        return autores.size();
    }

    @Override
    public Object getItem(int i) {
        return autores.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.lista_libros, null);

        ImageView imagen = (ImageView) view.findViewById(R.id.lista_biblioteca_imagen);
        TextView titulo = (TextView) view.findViewById(R.id.lista_biblioteca_tituloLibro);
        TextView autor = (TextView) view.findViewById(R.id.lista_biblioteca_autor);
        TextView fecha = (TextView) view.findViewById(R.id.lista_biblioteca_fecha);

        imagen.setImageResource(imagenes.get(i));
        titulo.setText(titulos.get(i));
        autor.setText(autores.get(i));
        fecha.setText(fechas.get(i));

        return view;
    }
}

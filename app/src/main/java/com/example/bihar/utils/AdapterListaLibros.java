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

    /**
     * Constructor del adapter para la lista de los libros
     * @param context: el contexto
     * @param imagenes: las imagenes
     * @param autores: los autores del libro
     * @param titulos: los titulos del libro
     * @param fechas: las fechas de publicacion
     */
    public AdapterListaLibros(Context context,ArrayList<Integer> imagenes, ArrayList<String> autores,
                              ArrayList<String> titulos, ArrayList<String> fechas) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imagenes = imagenes;
        this.autores = autores;
        this.titulos = titulos;
        this.fechas = fechas;
    }

    /**
     * Devuelve el número de libros que hay
     * @return número de libros
     */
    @Override
    public int getCount() {
        return autores.size();
    }

    /**
     * Devuelve el objeto de la posición i
     * @param i: la posición de la lista
     * @return: el objeto
     */
    @Override
    public Object getItem(int i) {
        return autores.get(i);
    }

    /**
     * Devuelve el identificador
     * @param i: identificador
     * @return: id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Devuelve el listView personalizado habiendole asignado valores
     * @param i: posición
     * @param view: la vista
     * @param viewGroup
     * @return
     */
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

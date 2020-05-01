package com.example.bihar.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bihar.R;

public class AdapterListaUniversidades extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private String[] universidades;
    private String[] disponibilidades;
    private boolean[] estanDisponibles;

    public AdapterListaUniversidades(Context context, String[] universidades,
                                     String[] disponibilidades, boolean[] estanDisponibles) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.universidades = universidades;
        this.disponibilidades = disponibilidades;
        this.estanDisponibles = estanDisponibles;
    }

    @Override
    public int getCount() {
        return universidades.length;
    }

    @Override
    public Object getItem(int i) {
        return universidades[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.lista_universidades_libro,null);

        TextView txtUni = (TextView) view.findViewById(R.id.lista_libroInformacion_universidad);
        TextView txtDisponible = (TextView) view.findViewById(R.id.lista_libroInformacion_disponibilidad);
        Button btnLocalizacion = (Button) view.findViewById(R.id.lista_libroInformacion_btnLocalizacion);
        Button btnReserva = (Button) view.findViewById(R.id.lista_libroInformacion_btnReservar);
        ImageView imageView = (ImageView) view.findViewById(R.id.lista_libroInformacion_imgDisponible);

        txtUni.setText(universidades[i]);

        if(estanDisponibles[i]){
            imageView.setImageResource(R.drawable.ic_libro_disponible);
            txtDisponible.setTextColor(context.getResources().getColor(R.color.verdeDisponible));
        }else{
            imageView.setImageResource(R.drawable.ic_libro_nodisponible);
            txtDisponible.setTextColor(Color.RED);
            btnReserva.setEnabled(false);
        }
        txtDisponible.setText(disponibilidades[i]);

        btnLocalizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }
}

package com.example.bihar.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.view.activities.Ajustes;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ToolBar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ToolBar extends Fragment {

    private TextView txtTitulo;
    private CircleImageView ajustes;
    public ToolBar() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_toolbar, container, false);

        ajustes = view.findViewById(R.id.imgAjustesToolbar);
        txtTitulo = view.findViewById(R.id.textView);

        ajustes.setOnClickListener( vista -> {
            Intent i = new Intent(getActivity(), Ajustes.class);
            startActivity(i);
        });

        return view;
    }

    public void cambiarTituloToolbar(String titulo){
        txtTitulo.setText(titulo);
    }

    public void ocultarAjustes(){
        ajustes.setVisibility(View.INVISIBLE);
    }
}

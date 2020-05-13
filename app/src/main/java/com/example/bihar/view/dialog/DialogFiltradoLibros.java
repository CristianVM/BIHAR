package com.example.bihar.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.bihar.R;

import java.util.ArrayList;

public class DialogFiltradoLibros extends DialogFragment {

    private String[] opciones;
    private ArrayList<String> seleccionados;
    private ListenerFiltradoLibros listenerFiltradoLibros;

    public interface ListenerFiltradoLibros {
        void temasSeleccionados(ArrayList<String> lista);
    }

    /**
     * Dialog que se encarga de seleccionar si quieres filtrar
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        opciones = new String[3];
        opciones[0] = getResources().getText(R.string.biblioteca_temaEconomia).toString();
        opciones[1] = getResources().getText(R.string.biblioteca_temaInformatica).toString();
        opciones[2] = getResources().getText(R.string.biblioteca_temaMedicina).toString();
        seleccionados = new ArrayList<>();
        listenerFiltradoLibros = (ListenerFiltradoLibros) getActivity();

        builder.setMultiChoiceItems(opciones, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b) {
                    seleccionados.add(opciones[i]);
                } else {
                    seleccionados.remove(opciones[i]);
                }
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listenerFiltradoLibros.temasSeleccionados(seleccionados);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        return builder.create();
    }
}

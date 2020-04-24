package com.example.bihar.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bihar.R;

import java.util.ArrayList;

public class DialogSelectAnioMatricula extends DialogFragment {

    private String[] anios;
    private ListenerSelectAnioMatricula listener;

    public interface ListenerSelectAnioMatricula{
        void seleccionAnio(int anio);
    }

    public DialogSelectAnioMatricula(ArrayList<String> aniosLista) {
        this.anios = new String[aniosLista.size()];
        this.anios = aniosLista.toArray(this.anios);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        listener = (ListenerSelectAnioMatricula) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getResources().getText(R.string.dialog_matricaTxtSelectAnio));

        builder.setItems(anios, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.seleccionAnio(i);
            }
        });
        return builder.create();
    }
}

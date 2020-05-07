package com.example.bihar.model;

import com.example.bihar.R;
import com.example.bihar.model.CardOpcion;

public class ListaOpcionesMenu {

    public static CardOpcion[] getListaOpcionesUsuario(){
        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_matricula,"matricula"),
                new CardOpcion(R.drawable.card_expediente,"expediente"),
                new CardOpcion(R.drawable.card_horarios,"horarios"),
                new CardOpcion(R.drawable.card_tutorias,"tutorias"),
                new CardOpcion(R.drawable.card_biblioteca, "biblioteca"),
                new CardOpcion(R.drawable.card_practicas,"practicas"),
                new CardOpcion(R.drawable.card_foro, "foro"),
                new CardOpcion(R.drawable.card_egela,"egela")};

    }

    public static CardOpcion[] getListaOpcionesExpediente(){
        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_asignaturas,"asignaturas"),
                new CardOpcion(R.drawable.card_creditos,"creditos"),
                new CardOpcion(R.drawable.card_logros,"logros"),
        };
    }

    public static CardOpcion[] getListaOpcionesProfesor(){
        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_foro, "foroProfesor"),
                new CardOpcion(R.drawable.card_matricula, "notasProfesor"),
                new CardOpcion(R.drawable.card_tutorias, "tutoriasProfesor"),
                new CardOpcion(R.drawable.card_egela,"egela"),
                new CardOpcion(R.drawable.card_biblioteca,"biblioteca")
        };
    }

}

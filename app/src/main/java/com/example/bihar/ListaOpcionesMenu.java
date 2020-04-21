package com.example.bihar;

public class ListaOpcionesMenu {

    public static CardOpcion[] getListaOpcionesUsuario(){
        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_matricula,"matricula"),
                new CardOpcion(R.drawable.card_expediente,"expediente"),
                new CardOpcion(R.drawable.card_horarios,"horarios"),
                new CardOpcion(R.drawable.card_tutorias,"tutorias"),
                new CardOpcion(R.drawable.card_biblioteca, "biblioteca"),
                new CardOpcion(R.drawable.card_practicas,"practicas"),
                new CardOpcion(R.drawable.card_egela,"Practicas")};

    }

}

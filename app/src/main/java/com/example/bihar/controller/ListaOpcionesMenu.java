package com.example.bihar.controller;

import com.example.bihar.R;
import com.example.bihar.model.CardOpcion;

/**
 * Clase que gestiona las opciones del menú principal y la acción que van a realizar
 */
public class ListaOpcionesMenu {

    public static CardOpcion[] getListaOpcionesUsuario(String idioma) {
        if (idioma.equals("eu")) {
            return new CardOpcion[]{
                    new CardOpcion(R.drawable.card_matricula_eus, "matricula"),
                    new CardOpcion(R.drawable.card_expediente_eus, "expediente"),
                    new CardOpcion(R.drawable.card_horarios_eus, "horarios"),
                    new CardOpcion(R.drawable.card_tutorias_eus, "tutorias"),
                    new CardOpcion(R.drawable.card_biblioteca_eus, "biblioteca"),
                    new CardOpcion(R.drawable.card_practicas_eus, "practicas"),
                    new CardOpcion(R.drawable.card_foro_eus, "foro"),
                    new CardOpcion(R.drawable.card_egela_eus, "egela")};
        }

        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_matricula, "matricula"),
                new CardOpcion(R.drawable.card_expediente, "expediente"),
                new CardOpcion(R.drawable.card_horarios, "horarios"),
                new CardOpcion(R.drawable.card_tutorias, "tutorias"),
                new CardOpcion(R.drawable.card_biblioteca, "biblioteca"),
                new CardOpcion(R.drawable.card_practicas, "practicas"),
                new CardOpcion(R.drawable.card_foro, "foro"),
                new CardOpcion(R.drawable.card_egela, "egela")};

    }

    public static CardOpcion[] getListaOpcionesExpediente(String idioma) {

        if (idioma.equals("eu")) {
            return new CardOpcion[]{
                    new CardOpcion(R.drawable.card_asignaturas_eus, "asignaturas"),
                    new CardOpcion(R.drawable.card_creditos_eus, "creditos"),
                    new CardOpcion(R.drawable.card_logros_eus, "logros"),
            };
        }

        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_asignaturas, "asignaturas"),
                new CardOpcion(R.drawable.card_creditos, "creditos"),
                new CardOpcion(R.drawable.card_logros, "logros"),
        };
    }

    public static CardOpcion[] getListaOpcionesProfesor(String idioma) {
        if (idioma.equals("eu")) {
            return new CardOpcion[]{
                    new CardOpcion(R.drawable.card_foro_eus, "foroProfesor"),
                    new CardOpcion(R.drawable.card_matricula_eus, "notasProfesor"),
                    new CardOpcion(R.drawable.card_tutorias_eus, "tutoriasProfesor"),
                    new CardOpcion(R.drawable.card_egela_eus, "egela"),
                    new CardOpcion(R.drawable.card_biblioteca_eus, "biblioteca")
            };
        }

        return new CardOpcion[]{
                new CardOpcion(R.drawable.card_foro, "foroProfesor"),
                new CardOpcion(R.drawable.card_matricula, "notasProfesor"),
                new CardOpcion(R.drawable.card_tutorias, "tutoriasProfesor"),
                new CardOpcion(R.drawable.card_egela, "egela"),
                new CardOpcion(R.drawable.card_biblioteca, "biblioteca")
        };

    }

}

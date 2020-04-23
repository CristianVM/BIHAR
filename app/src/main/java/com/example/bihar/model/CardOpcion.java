package com.example.bihar.model;

public class CardOpcion {

    private int imagen;
    private String tag;

    public CardOpcion(int pImagen, String pTag){
        imagen = pImagen;
        tag = pTag;
    }

    public int getImagen() {
        return imagen;
    }

    public String getTag() {
        return tag;
    }
}

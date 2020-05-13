package com.example.bihar.controller;

import com.example.bihar.model.Usuario;

/**
 * Clase singleton que gestiona el usuario que tiene la sesión iniciada
 */
public class GestorUsuario {

    private static GestorUsuario mGestorUsuario;

    private Usuario usuario;

    private GestorUsuario(){

    }

    public static GestorUsuario getGestorUsuario(){
        if(mGestorUsuario==null)
            mGestorUsuario = new GestorUsuario();
        return mGestorUsuario;
    }

    public void setUsuario(Usuario pUsuario){
        usuario = pUsuario;
    }

    public Usuario getUsuario(){
        return usuario;
    }
}

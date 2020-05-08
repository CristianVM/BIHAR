package com.example.bihar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Usuario {

    private String usuario;
    private String email;
    private String gmail;
    private double notaMedia;
    private int numCreditos;
    private HashMap<Integer, List<Asignatura>> asignaturas_por_curso;
    private HashMap<Integer, List<Asignatura>> asignaturas_por_anyo;

    public Usuario(String pUsuario, String pEmail){
        usuario = pUsuario;
        email = pEmail;
        asignaturas_por_curso = new HashMap<>();
        asignaturas_por_anyo = new HashMap<>();
    }

    public void setGmail(String pGmail){
        gmail = pGmail;
    }

    public void anadirAsignatura(Asignatura asignatura){
        int curso = asignatura.getCurso();
        int anyo = asignatura.getCurso();

        if(asignaturas_por_curso.containsKey(curso)){
            asignaturas_por_curso.get(curso).add(asignatura);
        }else{
            List<Asignatura> listaAsignaturas = new ArrayList<>();
            listaAsignaturas.add(asignatura);
            asignaturas_por_curso.put(curso,listaAsignaturas);
        }

        if(asignaturas_por_anyo.containsKey(anyo)){
            asignaturas_por_anyo.get(anyo).add(asignatura);
        }else{
            List<Asignatura> listaAsignaturas = new ArrayList<>();
            listaAsignaturas.add(asignatura);
            asignaturas_por_anyo.put(anyo,listaAsignaturas);
        }

    }

    public String getUsuario() {
        return usuario;
    }

    public String getEmail() {
        return email;
    }

    public String getGmail() {
        return gmail;
    }

    public HashMap<Integer, List<Asignatura>> getAsignaturas_por_curso() {
        return asignaturas_por_curso;
    }

    public HashMap<Integer, List<Asignatura>> getAsignaturas_por_anyo() {
        return asignaturas_por_anyo;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNotaMedia(double notaMedia) {
        this.notaMedia = notaMedia;
    }

    public void setNumCreditos(int numCreditos) {
        this.numCreditos = numCreditos;
    }

    public double getNotaMedia() {
        return notaMedia;
    }

    public int getNumCreditos() {
        return numCreditos;
    }
}

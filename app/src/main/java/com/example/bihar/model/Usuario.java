package com.example.bihar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Objeto que representa a un usuario, se establecerán los datos como el id, el email, la nota media
 * y el numero de cretitos del usuario al iniciar sesión.
 */

public class Usuario {

    private String idUsuario;
    private String email;
    private String gmail;
    private String foto64;
    private float notaMedia;
    private float numCreditos;
    private HashMap<Integer, List<Asignatura>> asignaturas_por_curso;
    private HashMap<Integer, List<Asignatura>> asignaturas_por_anyo;

    public Usuario(String pUsuario, String pEmail) {
        idUsuario = pUsuario;
        email = pEmail;
        asignaturas_por_curso = new HashMap<>();
        asignaturas_por_anyo = new HashMap<>();
    }

    public void setGmail(String pGmail) {
        gmail = pGmail;
    }

    public void setFoto64(String foto64) {
        this.foto64 = foto64;
    }

    public void anadirAsignatura(Asignatura asignatura) {
        int curso = asignatura.getCurso();
        int anyo = asignatura.getCurso();

        if (asignaturas_por_curso.containsKey(curso)) {
            asignaturas_por_curso.get(curso).add(asignatura);
        } else {
            List<Asignatura> listaAsignaturas = new ArrayList<>();
            listaAsignaturas.add(asignatura);
            asignaturas_por_curso.put(curso, listaAsignaturas);
        }

        if (asignaturas_por_anyo.containsKey(anyo)) {
            asignaturas_por_anyo.get(anyo).add(asignatura);
        } else {
            List<Asignatura> listaAsignaturas = new ArrayList<>();
            listaAsignaturas.add(asignatura);
            asignaturas_por_anyo.put(anyo, listaAsignaturas);
        }

    }

    public String getIdUsuario() {
        return idUsuario;
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

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNotaMedia(float notaMedia) {
        this.notaMedia = notaMedia;
    }

    public void setNumCreditos(float numCreditos) {
        this.numCreditos = numCreditos;
    }

    public float getNotaMedia() {
        return notaMedia;
    }

    public float getNumCreditos() {
        return numCreditos;
    }

    public void limpiarAsignaturas() {
        asignaturas_por_anyo.clear();
        asignaturas_por_curso.clear();
    }
}

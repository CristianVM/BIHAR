package com.example.bihar.controller;

import com.example.bihar.model.Libro;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestorLibros {

    private static GestorLibros gestorLibros;
    private Map<String, Libro> libros;

    /**
     * Constructor
     */
    private GestorLibros() {
        libros = new HashMap<>();
    }

    /**
     * Se obtiene el gestor de libros
     *
     * @return: gestor de libros
     */
    public static GestorLibros getGestorLibros() {
        if (gestorLibros == null) {
            gestorLibros = new GestorLibros();
        }
        return gestorLibros;
    }

    /**
     * Devuelve el mapa que contiene todos los libros
     *
     * @return
     */
    public Map<String, Libro> getLibros() {
        return libros;
    }

    /**
     * Añade los libros al map
     *
     * @param jsonString: El json en String con los datos de los libros
     */
    public void addLibro(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                if (!libros.containsKey((String) json.get("idLibro"))) {
                    libros.put((String) json.get("idLibro"), new Libro(
                            (String) json.get("titulo"),
                            (String) json.get("autor"),
                            (String) json.get("descripcion"),
                            (String) json.get("editorial"),
                            (String) json.get("fecha"),
                            (String) json.get("tema"),
                            (String) json.get("temaEuskera")));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Se obtiene la información de un libro en concreto
     *
     * @param idLibro: id del libro
     * @return: La información del libro
     */
    public Libro getInfoLibro(String idLibro) {
        return libros.get(idLibro);
    }

    /**
     * Se busca el libro por nombre del titulo
     *
     * @param busqueda: lo que se ha escrito en la búsqueda
     * @return: La lista de todos los libros que contienen lo escrito
     */
    public List<String> buscarLibro(String busqueda) {
        Set<String> idLibros = libros.keySet();
        List<String> filtro = new ArrayList<>();

        for (String id : idLibros) {
            String nombreLibro = libros.get(id).getTitulo();
            if (nombreLibro.toLowerCase().contains(busqueda.toLowerCase())) {
                filtro.add(id);
            }
        }
        return filtro;
    }

    /**
     * Se obtienen los libros dependiendo del tema elegido
     *
     * @param jsonFiltro: Los temas elegidos
     * @return: la lista con los libros que coincide con el tema escogido
     */
    public List<String> filtrarLibro(String jsonFiltro, String idioma) {
        Set<String> idLibros = libros.keySet();
        List<String> filtro = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(jsonFiltro);
            for (String id : idLibros) {
                String tema = "";
                if (idioma.equals("es")) {
                    tema = libros.get(id).getTema();
                }else{
                    tema = libros.get(id).getTemaEuskera();
                }
                if (tema.toLowerCase().equals(((String) json.get("filtroInformatica")).toLowerCase())
                        || tema.toLowerCase().equals(((String) json.get("filtroMedicina")).toLowerCase())
                        || tema.toLowerCase().equals(((String) json.get("filtroEconomia")).toLowerCase())) {
                    filtro.add(id);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return filtro;
    }

    /**
     * Vacía los libros
     */
    public void limpiarLibros() {
        libros.clear();
    }
}

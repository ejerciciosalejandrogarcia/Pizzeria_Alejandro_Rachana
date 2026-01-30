package Backend.Clases;

import java.util.*;

public class Pizza {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean disponible;
    private String tamano;
    private String tipoMasa;
    private String tipoSalsa;
    private List<String> ingredientes;
    private int tiempoPreparacion;

    public Pizza(int id, String nombre, String descripcion, double precio,
                 boolean disponible, String tamano, String tipoMasa, String tipoSalsa,
                 List<String> ingredientes, int tiempoPreparacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = disponible;
        this.tamano = tamano;
        this.tipoMasa = tipoMasa;
        this.tipoSalsa = tipoSalsa;
        this.ingredientes = new ArrayList<>(ingredientes);
        this.tiempoPreparacion = tiempoPreparacion;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public boolean getDisponible() {
        return disponible;
    }

    public String getTamano() {
        return tamano;
    }

    public String getTipoMasa() {
        return tipoMasa;
    }

    public String getTipoSalsa() {
        return tipoSalsa;
    }

    public List<String> getIngredientes() {
        return ingredientes;
    }

    public int getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    @Override
    public String toString() {
        return "Pizza ID: " + id +
                "\nNombre: " + nombre +
                "\nDescripción: " + descripcion +
                "\nPrecio: $" + precio +
                "\nDisponible: " + (disponible ? "Sí" : "No") +
                "\nTamaño: " + tamano +
                "\nTipo de Masa: " + tipoMasa +
                "\nTipo de Salsa: " + tipoSalsa +
                "\nIngredientes: " + String.join(", ", ingredientes) +
                "\nTiempo de Preparación: " + tiempoPreparacion + " minutos";
    }

    public void setId(int id) {
        this.id=id;
    }
}

package Backend.Clases;

import java.util.*;

public class Pedido {
    private static int contador = 0;

    private int id;
    private String numeroPedido;
    private String estado;
    private Date fechaPedido;
    private Date fechaEntrega;
    private Usuario usuario;
    private Map<Pizza, Integer> cantidadPizzas;
    private double subtotal;
    private double descuento;
    private double total;
    private String metodoPago;
    private boolean entregado;
    private String tipoEntrega;
    private String direccion;
    private String notas;

    public Pedido(int id, String numeroPedido, String estado, Date fechaPedido,
                  Date fechaEntrega, Usuario usuario, Map<Pizza, Integer> cantidadPizzas,
                  double subtotal, double descuento, double total, String metodoPago,
                  boolean entregado, String tipoEntrega, String direccion, String notas) {

        if (id == 0) {
            this.id = ++contador;
        } else {
            this.id = id;
        }

        this.numeroPedido = numeroPedido;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.fechaEntrega = fechaEntrega;
        this.usuario = usuario;
        this.cantidadPizzas = cantidadPizzas;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.metodoPago = metodoPago;
        this.entregado = entregado;
        this.tipoEntrega = tipoEntrega;
        this.direccion = direccion;
        this.notas = notas;
    }

    public static void setContador(int maxId) {
        contador = maxId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public String getEstado() {
        return estado;
    }

    public Date getFechaPedido() {
        return fechaPedido;
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public Cliente getCliente() {
        if (usuario instanceof Cliente) {
            return (Cliente) usuario;
        }
        return null;
    }

    public Map<Pizza, Integer> getCantidadPizzas() {
        return cantidadPizzas;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public boolean getEntregado() {
        return entregado;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getNotas() {
        return notas;
    }

    @Override
    public String toString() {
        return "Pedido ID: " + id +
                "\nNumero de Pedido: " + numeroPedido +
                "\nEstado: " + estado +
                "\nFecha Pedido: " + fechaPedido +
                "\nFecha Estimada de Entrega: " + fechaEntrega +
                "\nCliente: " + (usuario != null ? usuario.getNombre() : "N/A") +
                "\nPizzas y Cantidades: " + cantidadPizzas +
                "\nSubtotal: $" + subtotal +
                "\nDescuento: $" + descuento +
                "\nTotal: $" + total +
                "\nMetodo de Pago: " + metodoPago +
                "\nEntregado: " + (entregado ? "Si" : "No") +
                "\nTipo de Entrega: " + tipoEntrega +
                "\nDireccion de Entrega: " + (direccion != null ? direccion : "N/A") +
                "\nNotas: " + (notas != null ? notas : "Ninguna");
    }

    public void setId(int anInt) {
        this.id=id;
    }
}

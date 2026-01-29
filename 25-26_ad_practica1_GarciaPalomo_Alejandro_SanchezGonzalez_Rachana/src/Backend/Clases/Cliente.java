package Backend.Clases;

public class Cliente extends Usuario {
    private int tarjetaCredito;
    private Double descuento;


    // Constructor SIN contraseña (para deserializar)
    public Cliente(String nombre, String apellidos, int telefono, String email, int tarjetaCredito, String direccion) {
        super(nombre, apellidos, telefono, email);
        this.tarjetaCredito = tarjetaCredito;
        this.descuento = 10.0;
        this.setDireccion(direccion);  // ← USA LA DE LA CLASE PADRE
        this.rol = "CLIENTE";
    }

    // Constructor CON contraseña (para registro)
    public Cliente(String nombre, String apellidos, int telefono, String email, int tarjetaCredito, String direccion, String contrasena) {
        super(nombre, apellidos, telefono, email, contrasena);
        this.tarjetaCredito = tarjetaCredito;
        this.descuento = 10.0;
        this.setDireccion(direccion);  // ← USA LA DE LA CLASE PADRE
        this.rol = "CLIENTE";
    }

    // Constructor completo (para deserializar con ID)
    public Cliente(int id, String nombre, String apellidos, int telefono, String email, int tarjetaCredito, String direccion, Double descuento, String contrasena) {
        super(nombre, apellidos, telefono, email, contrasena);
        this.setId(id);
        this.tarjetaCredito = tarjetaCredito;
        this.descuento = descuento;
        this.setDireccion(direccion);  // ← USA LA DE LA CLASE PADRE
        this.rol = "CLIENTE";
    }



    // Getters
    public int getTarjetaCredito() {
        return tarjetaCredito;
    }

    public Double getDescuento() {
        return descuento;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", apellidos='" + getApellidos() + '\'' +
                ", telefono=" + getTelefono() +
                ", email='" + getEmail() + '\'' +
                ", tarjetaCredito=" + tarjetaCredito +
                ", descuento=" + descuento +
                ", direccion='" + getDireccion() + '\'' +
                '}';
    }

    public void setDescuento(double descuento) {
        this.descuento=descuento;
    }
}

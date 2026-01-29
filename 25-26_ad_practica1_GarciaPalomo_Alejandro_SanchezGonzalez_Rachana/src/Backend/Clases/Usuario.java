package Backend.Clases;

public class Usuario {

    private int id;
    private String nombre;
    private String apellidos;
    private int telefono;
    private String email;
    protected String rol;
    private String contrasena;
    private String direccion;

    // Constructor SIN contraseña (para deserializar desde fichero)
    public Usuario(String nombre, String apellidos, int telefono, String email) {
        this.id = 0;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.rol = "USUARIO";
        this.contrasena = null;
        this.direccion = null;
    }

    // Constructor CON contraseña (para registro)
    public Usuario(String nombre, String apellidos, int telefono, String email, String contrasena) {
        this.id = 0;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.rol = "USUARIO";
        this.contrasena = contrasena;
        this.direccion = null;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public String getApellidos() {
        return apellidos;
    }

    public int getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getDireccion() {
        return direccion;
    }

    // Setters
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", telefono=" + telefono +
                ", email='" + email + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}

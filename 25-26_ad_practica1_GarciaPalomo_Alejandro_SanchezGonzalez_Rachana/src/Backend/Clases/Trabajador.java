package Backend.Clases;

public class Trabajador extends Usuario {
    private String puesto;
    private Double salario;
    private String turno;

    // Constructor SIN contraseña (para deserializar)
    public Trabajador(String nombre, String apellidos, int telefono, String email, String puesto, Double salario, String turno) {
        super(nombre, apellidos, telefono, email);
        this.puesto = puesto;
        this.salario = salario;
        this.turno = turno;
        this.rol = "TRABAJADOR";
    }

    // Constructor CON contraseña (para registro)
    public Trabajador(String nombre, String apellidos, int telefono, String email, String puesto, Double salario, String turno, String contrasena) {
        super(nombre, apellidos, telefono, email, contrasena);
        this.puesto = puesto;
        this.salario = salario;
        this.turno = turno;
        this.rol = "TRABAJADOR";
    }

    // Constructor completo (para deserializar con ID)
    public Trabajador(int id, String nombre, String apellidos, int telefono, String email, String puesto, Double salario, String turno, String contrasena) {
        super(nombre, apellidos, telefono, email, contrasena);
        this.setId(id);
        this.puesto = puesto;
        this.salario = salario;
        this.turno = turno;
        this.rol = "TRABAJADOR";
    }

    // Getters
    public String getPuesto() {
        return puesto;
    }

    public Double getSalario() {
        return salario;
    }

    public String getTurno() {
        return turno;
    }

    // Setters
    public void setSalario(Double salario) {
        this.salario = salario;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    @Override
    public String toString() {
        return "Trabajador{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", apellidos='" + getApellidos() + '\'' +
                ", telefono=" + getTelefono() +
                ", email='" + getEmail() + '\'' +
                ", puesto='" + puesto + '\'' +
                ", salario=" + salario +
                ", turno='" + turno + '\'' +
                '}';
    }
}

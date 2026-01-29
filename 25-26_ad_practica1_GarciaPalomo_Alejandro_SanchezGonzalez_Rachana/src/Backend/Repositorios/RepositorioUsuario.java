package Backend.Repositorios;

import Backend.Clases.Cliente;
import Backend.Clases.Pedido;
import Backend.Clases.Trabajador;
import Backend.Clases.Usuario;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class RepositorioUsuario implements IRepositorioExtend<Usuario, Integer> {
    private final String archivo = "src/Backend/Base_de_datos/Usuarios.txt";

    private Connection conexion;

    // Constructor
    public RepositorioUsuario(Connection conexion) {
        this.conexion = conexion;
    }
    // ===== MÉTODOS DE IRepositorio =====

    @Override
    public Iterable<Usuario> findAll() {
        return findAllToList();
    }


    @Override
    public <S extends Usuario> S save(S entidad) {
        if (entidad == null) throw new IllegalArgumentException("Entidad no puede ser nula");

        try (Statement stmt = conexion.createStatement()) {

            // 1️⃣ Insertar en usuarios
            String sqlUsuario = "INSERT INTO usuarios(nombre, apellidos, telefono, email, direccion, contrasena, rol) " +
                    "VALUES (" +
                    "'" + entidad.getNombre().replace("'", "''") + "', " +
                    "'" + entidad.getApellidos().replace("'", "''") + "', " +
                    entidad.getTelefono() + ", " +
                    "'" + entidad.getEmail().replace("'", "''") + "', " +
                    "'" + (entidad.getDireccion() != null ? entidad.getDireccion().replace("'", "''") : "") + "', " +
                    "'" + (entidad.getContrasena() != null ? entidad.getContrasena().replace("'", "''") : "") + "', " +
                    "'" + (entidad instanceof Cliente ? "CLIENTE" : "TRABAJADOR") + "'" +
                    ")";
            stmt.executeUpdate(sqlUsuario, Statement.RETURN_GENERATED_KEYS);

            // Obtener el ID generado
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del usuario.");
                }
            }

            // 2️⃣ Insertar en tabla específica según rol
            if (entidad instanceof Cliente cliente) {
                String sqlCliente = "INSERT INTO clientes(id, tarjeta_credito, descuento) VALUES (" +
                        entidad.getId() + ", " +
                        cliente.getTarjetaCredito() + ", " +
                        cliente.getDescuento() +
                        ")";
                stmt.executeUpdate(sqlCliente);

            } else if (entidad instanceof Trabajador trabajador) {
                String sqlTrabajador = "INSERT INTO trabajadores(id, puesto, salario, turno) VALUES (" +
                        entidad.getId() + ", " +
                        "'" + trabajador.getPuesto().replace("'", "''") + "', " +
                        trabajador.getSalario() + ", " +
                        "'" + trabajador.getTurno().replace("'", "''") + "'" +
                        ")";
                stmt.executeUpdate(sqlTrabajador);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al guardar la entidad en la base de datos");
        }

        return entidad;
    }







    @Override
    public Usuario findById(Integer id) {
        return buscarTodosLista().stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsById(Integer id) {
        return buscarTodosLista().stream()
                .anyMatch(u -> u.getId() == id);
    }

    @Override
    public long count() {
        return buscarTodosLista().size();
    }

    @Override
    public void deleteById(Integer id) {
        List<Usuario> usuarios = buscarTodosLista();
        usuarios.removeIf(u -> u.getId() == id);
      //  guardarUsuarios(usuarios);
    }

    @Override
    public void deleteAll() {
        //guardarUsuarios(new ArrayList<>());
    }

    // ===== MÉTODOS DE IRepositorioExtend =====

    @Override
    public Optional<Usuario> findByIdOptional(Integer id) {
        return buscarTodosLista().stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    @Override
    public List<Usuario> findAllToList() {
        return buscarTodosLista();
    }

    @Override
    public <S extends Pedido> S actualizarPedido(S pedido) throws SQLException {
        return null;
    }

    // ===== MÉTODOS PRIVADOS Y AUXILIARES =====
    private List<Usuario> buscarTodosLista() {
        List<Usuario> usuarios = new ArrayList<>();

        try (Statement stmt = conexion.createStatement()) {

            // Clientes
            ResultSet rs = stmt.executeQuery(
                    "SELECT u.id, u.nombre, u.apellidos, u.telefono, u.email, u.direccion, u.contrasena, u.rol, " +
                            "c.tarjeta_credito, c.descuento " +
                            "FROM usuarios u JOIN clientes c ON u.id = c.id"
            );
            while (rs.next()) {
                Cliente c = new Cliente(
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getInt("telefono"),
                        rs.getString("email"),
                        rs.getInt("tarjeta_credito"),
                        rs.getString("direccion"),
                        rs.getString("contrasena")
                );
                c.setId(rs.getInt("id"));
                c.setRol(rs.getString("rol"));
                c.setDescuento(rs.getDouble("descuento"));
                usuarios.add(c);
            }
            rs.close();

            // Trabajadores
            rs = stmt.executeQuery(
                    "SELECT u.id, u.nombre, u.apellidos, u.telefono, u.email, u.direccion, u.contrasena, u.rol, " +
                            "t.puesto, t.salario, t.turno " +
                            "FROM usuarios u JOIN trabajadores t ON u.id = t.id"
            );
            while (rs.next()) {
                Trabajador t = new Trabajador(
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getInt("telefono"),
                        rs.getString("email"),
                        rs.getString("puesto"),
                        rs.getDouble("salario"),
                        rs.getString("turno"),
                        rs.getString("contrasena")
                );
                t.setId(rs.getInt("id"));
                t.setDireccion(rs.getString("direccion"));
                t.setRol(rs.getString("rol"));
                usuarios.add(t);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }

    public void actualizarUsuario(Usuario u) throws SQLException {
        if (u == null || u.getId() == 0)
            throw new IllegalArgumentException("Usuario inválido");

        try (Statement stmt = conexion.createStatement()) {

            // ======================
            // 🔹 usuarios
            // ======================
            String sqlUsuario = "UPDATE usuarios SET " +
                    "nombre='" + u.getNombre().replace("'", "''") + "'," +
                    "apellidos='" + u.getApellidos().replace("'", "''") + "'," +
                    "telefono=" + u.getTelefono() + "," +
                    "email='" + u.getEmail().replace("'", "''") + "'," +
                    "direccion='" + (u.getDireccion() != null ? u.getDireccion().replace("'", "''") : "") + "'" +
                    (u.getContrasena() != null ? ", contrasena='" + u.getContrasena() + "'" : "") +
                    " WHERE id=" + u.getId();

            stmt.executeUpdate(sqlUsuario);

            // ======================
            // 🔹 cliente
            // ======================
            if (u instanceof Cliente c) {
                String sqlCliente = "UPDATE clientes SET " +
                        "tarjeta_credito=" + c.getTarjetaCredito() + "," +
                        "descuento=" + c.getDescuento() +
                        " WHERE id=" + u.getId();
                stmt.executeUpdate(sqlCliente);
            }

            // ======================
            // 🔹 trabajador
            // ======================
            if (u instanceof Trabajador t) {
                String sqlTrabajador = "UPDATE trabajadores SET " +
                        "puesto='" + t.getPuesto().replace("'", "''") + "'," +
                        "salario=" + t.getSalario() + "," +
                        "turno='" + t.getTurno().replace("'", "''") + "' " +
                        "WHERE id=" + u.getId();
                stmt.executeUpdate(sqlTrabajador);
            }
        }
    }

}

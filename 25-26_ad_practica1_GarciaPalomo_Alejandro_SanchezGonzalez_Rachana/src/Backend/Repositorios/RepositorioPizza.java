package Backend.Repositorios;

import Backend.Clases.Pedido;
import Backend.Clases.Pizza;
import java.sql.*;
import java.util.*;

public class RepositorioPizza implements IRepositorioExtend<Pizza, Integer> {

    private Connection conexion;

    public RepositorioPizza(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public long count() {
        return buscarTodosLista().size();
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public Pizza findById(Integer id) {
        if (id == null) return null;

        Pizza pizza = null;

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pizzas WHERE id = " + id)) {

            if (rs.next()) {
                int pizzaId = rs.getInt("id");

                // Obtener ingredientes desde pizza_ingrediente
                List<String> ingredientes = new ArrayList<>();
                try (Statement stmtIng = conexion.createStatement();
                     ResultSet rsIng = stmtIng.executeQuery(
                             "SELECT ingrediente FROM pizza_ingrediente WHERE pizza_id = " + pizzaId)) {
                    while (rsIng.next()) {
                        ingredientes.add(rsIng.getString("ingrediente"));
                    }
                }

                pizza = new Pizza(
                        pizzaId,
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getBoolean("disponible"),
                        rs.getString("tamano"),
                        rs.getString("tipo_masa"),
                        rs.getString("tipo_salsa"),
                        ingredientes,
                        rs.getInt("tiempo_preparacion")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pizza;
    }

    @Override
    public Iterable<Pizza> findAll() {
        return buscarTodosLista();
    }

    @Override
    public Optional<Pizza> findByIdOptional(Integer id) {
        if (id == null) return Optional.empty();
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pizzas WHERE id=" + id)) {

            if (rs.next()) {
                Pizza pizza = new Pizza(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getBoolean("disponible"),
                        rs.getString("tamano"),
                        rs.getString("tipo_masa"),
                        rs.getString("tipo_salsa"),
                        Arrays.asList(rs.getString("ingredientes").split(",")),
                        rs.getInt("tiempo_preparacion")
                );
                return Optional.of(pizza);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Pizza> findAllToList() {
        return List.of();
    }

    @Override
    public <S extends Pedido> S actualizarPedido(S pedido) throws SQLException {
        return null;
    }

    @Override
    public <S extends Pizza> S save(S pizza) {
        if (pizza == null) return null;
        try (Statement stmt = conexion.createStatement()) {
            if (pizza.getId() == 0) {
                String sql = "INSERT INTO pizzas(nombre, descripcion, precio, disponible, tamano, tipo_masa, tipo_salsa, ingredientes, tiempo_preparacion) VALUES (" +
                        "'" + pizza.getNombre() + "'," +
                        "'" + pizza.getDescripcion() + "'," +
                        pizza.getPrecio() + "," +
                        pizza.getDisponible() + "," +
                        "'" + pizza.getTamano() + "'," +
                        "'" + pizza.getTipoMasa() + "'," +
                        "'" + pizza.getTipoSalsa() + "'," +
                        "'" + String.join(",", pizza.getIngredientes()) + "'," +
                        pizza.getTiempoPreparacion() + ")";
                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) pizza.setId(rs.getInt(1));
                }
            } else {
                // UPDATE...
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pizza;
    }

    // ========================
    // MÉTODOS PROPIOS
    // ========================

    private List<Pizza> buscarTodosLista() {
        List<Pizza> pizzas = new ArrayList<>();
        if (conexion == null) {
            System.err.println("❌ Conexión no inicializada");
            return pizzas;
        }

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pizzas")) {

            while (rs.next()) {
                int pizzaId = rs.getInt("id");
                // Obtener ingredientes de pizza_ingrediente
                List<String> ingredientes = new ArrayList<>();
                try (Statement stmtIng = conexion.createStatement();
                     ResultSet rsIng = stmtIng.executeQuery(
                             "SELECT ingrediente FROM pizza_ingrediente WHERE pizza_id = " + pizzaId)) {
                    while (rsIng.next()) {
                        ingredientes.add(rsIng.getString("ingrediente"));
                    }
                }

                pizzas.add(new Pizza(
                        pizzaId,
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getBoolean("disponible"),
                        rs.getString("tamano"),
                        rs.getString("tipo_masa"),
                        rs.getString("tipo_salsa"),
                        ingredientes,
                        rs.getInt("tiempo_preparacion")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pizzas;
    }


} // <-- cierra la clase solo al final

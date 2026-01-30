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
    public void deleteById(Integer id) {
        if (id == null) return;

        try {
            conexion.setAutoCommit(false);

            // borrar ingredientes
            try (PreparedStatement ps1 =
                         conexion.prepareStatement(
                                 "DELETE FROM pizza_ingrediente WHERE pizza_id = ?")) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }

            // borrar pizza
            try (PreparedStatement ps2 =
                         conexion.prepareStatement(
                                 "DELETE FROM pizzas WHERE id = ?")) {
                ps2.setInt(1, id);
                ps2.executeUpdate();
            }

            conexion.commit();

        } catch (SQLException e) {
            try { conexion.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
        } finally {
            try { conexion.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }


    @Override
    public void deleteAll() {
        if (conexion == null) return;

        try {
            conexion.setAutoCommit(false); // Iniciar transacción

            try (Statement stmt = conexion.createStatement()) {
                // Borrar todos los ingredientes
                stmt.executeUpdate("DELETE FROM pizza_ingrediente");

                // Borrar todas las pizzas
                stmt.executeUpdate("DELETE FROM pizzas");
            }

            conexion.commit(); // Confirmar cambios
            System.out.println("✅ Todas las pizzas han sido eliminadas.");

        } catch (SQLException e) {
            try {
                conexion.rollback(); // Revertir si hay error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conexion.setAutoCommit(true); // Restaurar autocommit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean existsById(Integer id) {
        if (id == null) return false;

        try (PreparedStatement ps =
                     conexion.prepareStatement("SELECT 1 FROM pizzas WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        return buscarTodosLista();
    }

    @Override
    public <S extends Pedido> S actualizarPedido(S pedido) throws SQLException {
        if (pedido == null || pedido.getId() == 0) return null;

        try {
            conexion.setAutoCommit(false); // Inicio de transacción

            try (Statement stmt = conexion.createStatement()) {

                // 1️⃣ Actualizar la tabla pedidos
                String sqlActualizarPedido = "UPDATE pedidos SET " +
                        "numero_pedido = '" + pedido.getNumeroPedido() + "', " +
                        "estado = '" + pedido.getEstado() + "', " +
                        "fecha_pedido = '" + new java.sql.Timestamp(pedido.getFechaPedido().getTime()) + "', " +
                        "fecha_entrega = " + (pedido.getFechaEntrega() == null ? "NULL" : "'" + new java.sql.Timestamp(pedido.getFechaEntrega().getTime()) + "'") + ", " +
                        "usuario_id = " + pedido.getUsuario().getId() + ", " +
                        "subtotal = " + pedido.getSubtotal() + ", " +
                        "descuento = " + pedido.getDescuento() + ", " +
                        "total = " + pedido.getTotal() + ", " +
                        "metodo_pago = '" + pedido.getMetodoPago() + "', " +
                        "entregado = " + (pedido.getEntregado() ? 1 : 0) + ", " +
                        "tipo_entrega = '" + pedido.getTipoEntrega() + "', " +
                        "direccion = '" + (pedido.getDireccion() == null ? "" : pedido.getDireccion()) + "', " +
                        "notas = '" + (pedido.getNotas() == null ? "" : pedido.getNotas()) + "' " +
                        "WHERE id = " + pedido.getId();

                stmt.executeUpdate(sqlActualizarPedido);

                // 2️⃣ Borrar todas las pizzas antiguas del pedido
                stmt.executeUpdate("DELETE FROM pedidos_pizza WHERE pedido_id = " + pedido.getId());

                // 3️⃣ Insertar pizzas nuevas del pedido
                for (Map.Entry<Pizza, Integer> entry : pedido.getCantidadPizzas().entrySet()) {
                    Pizza pizza = entry.getKey();
                    int cantidad = entry.getValue();

                    String sqlInsertPizza = "INSERT INTO pedidos_pizza(pedido_id, pizza_id, cantidad, precio_unitario) VALUES (" +
                            pedido.getId() + ", " + pizza.getId() + ", " + cantidad + ", " + pizza.getPrecio() + ")";
                    stmt.addBatch(sqlInsertPizza);
                }

                stmt.executeBatch(); // Ejecutar todos los inserts de pizzas

            }

            conexion.commit(); // Confirmar todos los cambios
        } catch (SQLException e) {
            try {
                conexion.rollback(); // Deshacer todo si hay error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return null;
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return pedido;
    }


    @Override
    public <S extends Pizza> S save(S pizza) {
        if (pizza == null) return null;

        try {
            conexion.setAutoCommit(false); // Inicio de transacción

            if (pizza.getId() == 0) {
                // INSERT pizza
                String sql = "INSERT INTO pizzas(nombre, descripcion, precio, disponible, tamano, tipo_masa, tipo_salsa, tiempo_preparacion) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, pizza.getNombre());
                    ps.setString(2, pizza.getDescripcion());
                    ps.setDouble(3, pizza.getPrecio());
                    ps.setBoolean(4, pizza.getDisponible());
                    ps.setString(5, pizza.getTamano());
                    ps.setString(6, pizza.getTipoMasa());
                    ps.setString(7, pizza.getTipoSalsa());
                    ps.setInt(8, pizza.getTiempoPreparacion());

                    int affectedRows = ps.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("No se pudo insertar la pizza, ninguna fila afectada.");
                    }

                    // Obtener el ID generado
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            pizza.setId(rs.getInt(1));
                        } else {
                            throw new SQLException("No se pudo obtener el ID generado de la pizza.");
                        }
                    }
                }

                // INSERT ingredientes ahora que pizza tiene ID
                String sqlIng = "INSERT INTO pizza_ingrediente(pizza_id, ingrediente) VALUES (?, ?)";
                try (PreparedStatement psIng = conexion.prepareStatement(sqlIng)) {
                    for (String ing : pizza.getIngredientes()) {
                        psIng.setInt(1, pizza.getId()); // ID ya generado
                        psIng.setString(2, ing.trim());
                        psIng.addBatch();
                    }
                    psIng.executeBatch();
                }

            } else {
                // UPDATE pizza existente
                String sql = "UPDATE pizzas SET nombre=?, descripcion=?, precio=?, disponible=?, tamano=?, tipo_masa=?, tipo_salsa=?, tiempo_preparacion=? " +
                        "WHERE id=?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, pizza.getNombre());
                    ps.setString(2, pizza.getDescripcion());
                    ps.setDouble(3, pizza.getPrecio());
                    ps.setBoolean(4, pizza.getDisponible());
                    ps.setString(5, pizza.getTamano());
                    ps.setString(6, pizza.getTipoMasa());
                    ps.setString(7, pizza.getTipoSalsa());
                    ps.setInt(8, pizza.getTiempoPreparacion());
                    ps.setInt(9, pizza.getId());
                    ps.executeUpdate();
                }

                // Borrar y volver a insertar ingredientes
                try (PreparedStatement psDel = conexion.prepareStatement(
                        "DELETE FROM pizza_ingrediente WHERE pizza_id=?")) {
                    psDel.setInt(1, pizza.getId());
                    psDel.executeUpdate();
                }

                String sqlIng = "INSERT INTO pizza_ingrediente(pizza_id, ingrediente) VALUES (?, ?)";
                try (PreparedStatement psIng = conexion.prepareStatement(sqlIng)) {
                    for (String ing : pizza.getIngredientes()) {
                        psIng.setInt(1, pizza.getId());
                        psIng.setString(2, ing.trim());
                        psIng.addBatch();
                    }
                    psIng.executeBatch();
                }
            }

            conexion.commit(); // Confirmar todo

        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

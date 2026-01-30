package Backend.Repositorios;

import Backend.Clases.Pedido;
import Backend.Clases.Pizza;
import Backend.Clases.Usuario;

import java.sql.*;
import java.util.*;

public class RepositorioPedido implements IRepositorioExtend<Pedido, Integer> {

    private Connection conexion;
    private RepositorioPizza repoPizza;
    private RepositorioUsuario repoUsuarios;

    // Constructor
    public RepositorioPedido(Connection conexion) {
        this.conexion = conexion;
        this.repoPizza = new RepositorioPizza(conexion);
        this.repoUsuarios = new RepositorioUsuario(conexion);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM pedidos";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID no puede ser nulo");
        String sql = "DELETE FROM pedidos WHERE id = " + id;
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate(sql);
            // También eliminar relación con pizzas en tabla pedidos_pizzas si existe
            stmt.executeUpdate("DELETE FROM pedidos_pizzas WHERE pedido_id=" + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate("DELETE FROM pedidos");
            stmt.executeUpdate("DELETE FROM pedidos_pizzas");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsById(Integer id) {
        return findByIdOptional(id).isPresent();
    }

    @Override
    public Pedido findById(Integer id) {
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Optional<Pedido> findByIdOptional(Integer id) {
        if (id == null) return Optional.empty();

        String sql = "SELECT * FROM pedidos WHERE id=" + id;
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                Usuario usuario = repoUsuarios.findById(rs.getInt("usuario_id"));
                Map<Pizza, Integer> pizzas = cargarPizzas(rs.getInt("id"));

                Pedido pedido = new Pedido(
                        rs.getInt("id"),
                        rs.getString("numero_pedido"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_pedido"),
                        rs.getTimestamp("fecha_entrega"),
                        usuario,
                        pizzas,
                        rs.getDouble("subtotal"),
                        rs.getDouble("descuento"),
                        rs.getDouble("total"),
                        rs.getString("metodo_pago"),
                        rs.getBoolean("entregado"),
                        rs.getString("tipo_entrega"),
                        rs.getString("direccion"),
                        rs.getString("notas")
                );

                return Optional.of(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // ============================
// Actualizar un pedido existente
// ============================
    public <S extends Pedido> S actualizarPedido(S pedido) throws SQLException {
        if (pedido == null || pedido.getId() == 0)
            throw new IllegalArgumentException("Pedido inválido para actualizar");

        conexion.setAutoCommit(false);

        try (Statement stmt = conexion.createStatement()) {
            // UPDATE en pedidos
            String sqlUpdate = "UPDATE pedidos SET " +
                    "numero_pedido='" + pedido.getNumeroPedido().replace("'", "''") + "'," +
                    "estado='" + pedido.getEstado().replace("'", "''") + "'," +
                    "fecha_pedido='" + new Timestamp(pedido.getFechaPedido().getTime()) + "'," +
                    "fecha_entrega=" + (pedido.getFechaEntrega() != null ? "'" + new Timestamp(pedido.getFechaEntrega().getTime()) + "'" : "NULL") + "," +
                    "usuario_id=" + pedido.getUsuario().getId() + "," +
                    "subtotal=" + pedido.getSubtotal() + "," +
                    "descuento=" + pedido.getDescuento() + "," +
                    "total=" + pedido.getTotal() + "," +
                    "metodo_pago='" + pedido.getMetodoPago().replace("'", "''") + "'," +
                    "entregado=" + pedido.getEntregado() + "," +
                    "tipo_entrega='" + pedido.getTipoEntrega().replace("'", "''") + "'," +
                    "direccion='" + (pedido.getDireccion() != null ? pedido.getDireccion().replace("'", "''") : "") + "'," +
                    "notas='" + (pedido.getNotas() != null ? pedido.getNotas().replace("'", "''") : "") + "' " +
                    "WHERE id=" + pedido.getId();

            int filas = stmt.executeUpdate(sqlUpdate);
            if (filas == 0) {
                throw new SQLException("Pedido con id " + pedido.getId() + " no existe para actualizar");
            }

            // Limpiar relaciones anteriores
            stmt.executeUpdate("DELETE FROM pedidos_pizzas WHERE pedido_id=" + pedido.getId());

            // Insertar nuevas relaciones
            for (Map.Entry<Pizza, Integer> entry : pedido.getCantidadPizzas().entrySet()) {
                double precioUnitario = entry.getKey().getPrecio();
                String sqlRelacion = "INSERT INTO pedidos_pizzas(pedido_id, pizza_id, cantidad, precio_unitario) VALUES (" +
                        pedido.getId() + "," +
                        entry.getKey().getId() + "," +
                        entry.getValue() + "," +
                        precioUnitario + ")";
                stmt.executeUpdate(sqlRelacion);
            }

            conexion.commit();
        } catch (SQLException e) {
            conexion.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }

        return pedido;
    }

    @Override
    public Iterable<Pedido> findAll() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id FROM pedidos";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                findByIdOptional(rs.getInt("id")).ifPresent(pedidos::add);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    @Override
    public List<Pedido> findAllToList() {
        return new ArrayList<>((Collection<? extends Pedido>) findAll());
    }

    // ============================
    // Guardar un nuevo pedido
    // ============================
    @Override
    public <S extends Pedido> S save(S pedido) throws SQLException {

        if (pedido == null)
            throw new IllegalArgumentException("Pedido no puede ser nulo");

        if (pedido.getId() != 0)
            throw new IllegalArgumentException(
                    "El pedido ya tiene ID, usa actualizarPedido() para actualizarlo"
            );

        conexion.setAutoCommit(false);

        try {

            String sqlInsert = "INSERT INTO pedidos " +
                    "(numero_pedido, estado, fecha_pedido, fecha_entrega, usuario_id, subtotal, descuento, total, metodo_pago, entregado, tipo_entrega, direccion, notas) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conexion.prepareStatement(
                    sqlInsert,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, pedido.getNumeroPedido());
            ps.setString(2, pedido.getEstado());
            ps.setTimestamp(3, new Timestamp(pedido.getFechaPedido().getTime()));
            ps.setTimestamp(4,
                    pedido.getFechaEntrega() != null
                            ? new Timestamp(pedido.getFechaEntrega().getTime())
                            : null
            );
            ps.setInt(5, pedido.getUsuario().getId());
            ps.setDouble(6, pedido.getSubtotal());
            ps.setDouble(7, pedido.getDescuento());
            ps.setDouble(8, pedido.getTotal());
            ps.setString(9, pedido.getMetodoPago());
            ps.setBoolean(10, pedido.getEntregado());
            ps.setString(11, pedido.getTipoEntrega());
            ps.setString(12, pedido.getDireccion());
            ps.setString(13, pedido.getNotas());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setId(rs.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID generado del pedido");
                }
            }

            for (Map.Entry<Pizza, Integer> entry : pedido.getCantidadPizzas().entrySet()) {

                Pizza pizza = entry.getKey();

                if (pizza.getId() == 0) {
                    continue;
                }

                String sqlRelacion =
                        "INSERT INTO pedidos_pizzas(pedido_id, pizza_id, cantidad, precio_unitario) " +
                                "VALUES (?, ?, ?, ?)";

                PreparedStatement psRel = conexion.prepareStatement(sqlRelacion);
                psRel.setInt(1, pedido.getId());
                psRel.setInt(2, pizza.getId());
                psRel.setInt(3, entry.getValue());
                psRel.setDouble(4, pizza.getPrecio());

                psRel.executeUpdate();
            }

            conexion.commit();

        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }

        return pedido;
    }



    // ============================
    // MÉTODOS AUXILIARES
    // ============================

    private Map<Pizza, Integer> cargarPizzas(int pedidoId) {
        Map<Pizza, Integer> pizzas = new HashMap<>();
        String sql = "SELECT pizza_id, cantidad FROM pedidos_pizzas WHERE pedido_id=" + pedidoId;
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Pizza pizza = repoPizza.findById(rs.getInt("pizza_id"));
                if (pizza != null) {
                    pizzas.put(pizza, rs.getInt("cantidad"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pizzas;
    }

    public List<Pedido> buscarPedidosPorClienteId(int clienteId) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id FROM pedidos WHERE usuario_id=" + clienteId;
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                findByIdOptional(rs.getInt("id")).ifPresent(pedidos::add);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    public List<Pedido> buscarPedidosPorEstado(String estado) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id FROM pedidos WHERE estado='" + estado + "'";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                findByIdOptional(rs.getInt("id")).ifPresent(pedidos::add);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }
}

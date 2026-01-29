package Backend.Repositorios;

import Backend.Clases.Pedido;

import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public interface IRepositorioExtend<T, ID> extends IRepositorio<T, ID> {
    /**
     * Devuelve la entidad T con identificador id.
     *
     * @param id    Identificador de la entidad
     * @return    Entidad que tiene como identificador id u Optional#empty() si no se encuentra
     *
     * @throws IllegalArgumentException En caso de ser id nulo
     */
    Optional<T> findByIdOptional(ID id);

    /**
     * Devuelve todas las instancias de tipo T
     *
     * @return   todas las instancias.
     */
    List<T> findAllToList();

    // ============================
// Actualizar un pedido existente
// ============================
    <S extends Pedido> S actualizarPedido(S pedido) throws SQLException;
}
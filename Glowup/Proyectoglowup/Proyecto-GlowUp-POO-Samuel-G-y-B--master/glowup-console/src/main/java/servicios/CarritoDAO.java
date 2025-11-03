package servicios;

import config.Database;
import comercio.Carrito;
import comercio.LineaCarrito;
import produccion.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static config.Database.getConnection;

public class CarritoDAO {
    private final ProductDAO productDAO = new ProductDAO();

    private static final String SQL_BUSCAR_CARRITO_POR_CLIENTE = """
        SELECT * FROM carrito WHERE cliente_id = ?
    """;

    private static final String SQL_INSERTAR_CARRITO = """
        INSERT INTO carrito (id, cliente_id) VALUES (?, ?)
    """;

    private static final String SQL_BUSCAR_LINEAS_POR_CARRITO = """
        SELECT lc.*, p.nombre, p.precio, p.descripcion, p.stock, p.fecha_lanzamiento, p.categoria_id
        FROM carrito_linea_item lc 
        JOIN producto p ON lc.producto_id = p.id 
        WHERE lc.carrito_id = ?
    """;

    private static final String SQL_INSERTAR_LINEA = """
        INSERT INTO carrito_linea_item (carrito_id, producto_id, cantidad) 
        VALUES (?, ?, ?)
    """;

    private static final String SQL_ACTUALIZAR_LINEA = """
        UPDATE carrito_linea_item 
        SET cantidad = ? 
        WHERE carrito_id = ? AND producto_id = ?
    """;

    private static final String SQL_ELIMINAR_LINEA = """
        DELETE FROM carrito_linea_item 
        WHERE carrito_id = ? AND producto_id = ?
    """;

    private static final String SQL_ELIMINAR_TODAS_LINEAS = """
        DELETE FROM carrito_linea_item WHERE carrito_id = ?
    """;

    private static final String SQL_BUSCAR_LINEA = """
        SELECT * FROM carrito_linea_item 
        WHERE carrito_id = ? AND producto_id = ?
    """;

    public Carrito obtenerOCrearCarritoPorClienteId(String clienteId) {
        try (Connection con = getConnection()) {
            Optional<Carrito> carritoOpt = buscarCarritoPorClienteId(clienteId, con);

            if (carritoOpt.isPresent()) {
                Carrito carrito = carritoOpt.get();
                List<LineaCarrito> lineas = cargarLineas(carrito.getId(), con);

                carrito.limpiar();
                lineas.forEach(linea -> carrito.agregarProducto(linea.getProducto(), linea.getCantidad()));

                System.out.println("[CARRITO DAO] Carrito cargado desde BD para cliente: " + clienteId);
                return carrito;
            } else {
                System.out.println("[CARRITO DAO] Creando nuevo carrito para cliente: " + clienteId);
                return insertarCarritoNuevo(clienteId, con);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo o creando el carrito: " + e.getMessage(), e);
        }
    }

    public void guardarLineaCarrito(String carritoId, String productoId, int cantidad) {
        String sql = "INSERT INTO carrito_linea_item (id, carrito_id, producto_id, cantidad) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString()); // ← AQUÍ GENERAS EL ID
            stmt.setString(2, carritoId);
            stmt.setString(3, productoId);
            stmt.setInt(4, cantidad);

            stmt.executeUpdate();

            System.out.println("[CARRITO DAO] Línea guardada - Carrito: " + carritoId + ", Producto: " + productoId + ", Cantidad: " + cantidad);

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando línea de carrito: " + e.getMessage(), e);
        }
    }

    public void eliminarLinea(String carritoId, String productoId) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR_LINEA)) {

            ps.setString(1, carritoId);
            ps.setString(2, productoId);
            ps.executeUpdate();

            System.out.println("[CARRITO DAO] Línea eliminada - Carrito: " + carritoId + ", Producto: " + productoId);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando línea de carrito: " + e.getMessage(), e);
        }
    }

    public void limpiarCarrito(String carritoId) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR_TODAS_LINEAS)) {

            ps.setString(1, carritoId);
            ps.executeUpdate();
            System.out.println("[CARRITO DAO] Carrito limpiado: " + carritoId);
        } catch (SQLException e) {
            throw new RuntimeException("Error limpiando el carrito: " + e.getMessage(), e);
        }
    }

    private Optional<Carrito> buscarCarritoPorClienteId(String clienteId, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_CARRITO_POR_CLIENTE)) {
            ps.setString(1, clienteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Carrito(rs.getString("id")));
            }
        }
        return Optional.empty();
    }

    private Carrito insertarCarritoNuevo(String clienteId, Connection con) throws SQLException {
        String nuevoId = "cart-" + UUID.randomUUID().toString();
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_CARRITO)) {
            ps.setString(1, nuevoId);
            ps.setString(2, clienteId);
            ps.executeUpdate();
            return new Carrito(nuevoId);
        }
    }

    private List<LineaCarrito> cargarLineas(String carritoId, Connection con) throws SQLException {
        List<LineaCarrito> lineas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_LINEAS_POR_CARRITO)) {
            ps.setString(1, carritoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String productoId = rs.getString("producto_id");
                int cantidad = rs.getInt("cantidad");

                Optional<Producto> productoOpt = productDAO.buscarPorId(productoId);
                if (productoOpt.isPresent()) {
                    lineas.add(new LineaCarrito(productoOpt.get(), cantidad));
                } else {
                    System.out.println("[CARRITO DAO] Producto no encontrado, omitiendo: " + productoId);
                }
            }
        }
        return lineas;
    }

    private boolean existeLinea(String carritoId, String productoId, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_LINEA)) {
            ps.setString(1, carritoId);
            ps.setString(2, productoId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
}